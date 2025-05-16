import {getCLS, getFCP, getFID, getLCP, getTTFB} from 'web-vitals';

const MODE= import.meta.env.MODE || 'development';
const pushUrl = window.__METRICS_PUSH_ENDPOINT__
    || import.meta.env.VITE_METRICS_PUSH_ENDPOINT
    || 'http://pushgateway:9091';
const FLUSH_EVERY = 15_000;

const C = {};
const S = {};

const k = (name, lab={}) => name+JSON.stringify(lab);

export const inc = (name, lab={}, n=1) =>
    (C[k(name,lab)] = (C[k(name,lab)]||0) + n);

export const observe = (name, val, lab={}) => {
    const id = k(name,lab);
    (S[id] = S[id] || []).push(val);
};

export const setGauge = (name, val, lab={}) => (C[k(name,lab)] = val);

const line = (metric, lab, val) => {
    const lbl = Object.keys(lab).length
        ? `{${Object.entries(lab).map(([k,v])=>`${k}="${v}"`).join(',')}}`
        : '';
    return `${metric}${lbl} ${val}`;
};

function flush(){
    const rows = [];

    for(const [id,val] of Object.entries(C)){
        const [metric,lbl] = id.match(/^([^{]+)(.*)$/).slice(1);
        rows.push(line(metric, JSON.parse(lbl), val));
        delete C[id];
    }

    for(const [id,arr] of Object.entries(S)){
        const [base,lbl] = id.match(/^([^{]+)(.*)$/).slice(1);
        const lab = JSON.parse(lbl);
        rows.push(line(`${base}_count`, lab, arr.length));
        rows.push(line(`${base}_sum`,   lab, arr.reduce((a,b)=>a+b,0)));
        delete S[id];
    }

    if(rows.length){
        navigator.sendBeacon(
            `${pushUrl}/metrics/job/front/instance/${encodeURIComponent(location.hostname)}`,
            rows.join('\n')
        );
    }
}

setInterval(flush, FLUSH_EVERY);
addEventListener('beforeunload', flush);

const base = { mode: MODE };

getCLS (r=>observe('web_vitals_cls_seconds', r.value, base));
getFID (r=>observe('web_vitals_fid_ms',      r.value, base));
getLCP (r=>observe('web_vitals_lcp_ms',      r.value, base));
getFCP (r=>observe('web_vitals_fcp_ms',      r.value, base));
getTTFB(r=>observe('web_vitals_ttfb_ms',     r.value, base));

inc('page_view_total', { path: location.pathname });

setGauge('page_timing_dom_ms', performance.timing.domComplete - performance.timing.domLoading, base);
