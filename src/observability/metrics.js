import * as vitals from 'web-vitals';

const MODE = import.meta.env.MODE || 'development';
const PUSH_ENDPOINT_ENV = import.meta.env.VITE_METRICS_PUSH_ENDPOINT;
const PUSH_ENDPOINT = PUSH_ENDPOINT_ENV || '';
const METRICS_DISABLED = MODE !== 'production' || !PUSH_ENDPOINT;
const FLUSH_INTERVAL_MS = 15_000;

const Counters = Object.create(null);
const Summaries = Object.create(null);

const makeId = (name, labels = {}) => name + JSON.stringify(labels);

const _inc = (name, labels = {}, n = 1) => {
    const id = makeId(name, labels);
    Counters[id] = (Counters[id] || 0) + n;
};
const _observe = (name, value, labels = {}) => {
    const id = makeId(name, labels);
    Summaries[id] = Summaries[id] || [];
    Summaries[id].push(value);
};
export const setGauge = (name, value, labels = {}) => {
    const id = makeId(name, labels);
    Counters[id] = value;
};

export const inc = _inc;
export const observe = _observe;
export const incCounter = _inc;
export const observeHistogram = _observe;

const formatLine = (metric, labels, value) => {
    const lbl = Object.keys(labels).length
        ? `{${Object.entries(labels).map(([k, v]) => `${k}="${v}"`).join(',')}}`
        : '';
    return `${metric}${lbl} ${value}`;
};

function flush() {
    if (METRICS_DISABLED) return;

    const rows = [];

    for (const [id, val] of Object.entries(Counters)) {
        const [metric, lblRaw] = id.match(/^([^{]+)(.*)$/).slice(1);
        const labels = JSON.parse(lblRaw || '{}');
        rows.push(formatLine(metric, labels, val));
        delete Counters[id];
    }

    for (const [id, arr] of Object.entries(Summaries)) {
        const [base, lblRaw] = id.match(/^([^{]+)(.*)$/).slice(1);
        const labels = JSON.parse(lblRaw || '{}');
        rows.push(formatLine(`${base}_count`, labels, arr.length));
        rows.push(formatLine(`${base}_sum`, labels, arr.reduce((a, b) => a + b, 0)));
        delete Summaries[id];
    }

    if (rows.length) {
        try {
            navigator.sendBeacon(
                `${PUSH_ENDPOINT}/metrics/job/front/instance/${encodeURIComponent(location.hostname)}`,
                rows.join('\n')
            );
        } catch (e) {
            console.warn('Metrics flush failed:', e);
        }
    }
}

setInterval(flush, FLUSH_INTERVAL_MS);
addEventListener('beforeunload', flush);

const baseLabels = {mode: MODE};
vitals.getCLS(r => observe('web_vitals_cls_seconds', r.value, baseLabels));
vitals.getFID(r => observe('web_vitals_fid_ms', r.value, baseLabels));
vitals.getLCP(r => observe('web_vitals_lcp_ms', r.value, baseLabels));
vitals.getFCP(r => observe('web_vitals_fcp_ms', r.value, baseLabels));
vitals.getTTFB(r => observe('web_vitals_ttfb_ms', r.value, baseLabels));

inc('page_view_total', {path: location.pathname});
setGauge(
    'page_timing_dom_ms',
    performance.timing.domComplete - performance.timing.domLoading,
    baseLabels
);
