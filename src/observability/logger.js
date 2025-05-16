import log        from 'loglevel';
import { nanoid } from 'nanoid';

const MODE       = import.meta.env.MODE || 'development';
const queryLvl   = new URLSearchParams(location.search).get('log');
const logLevel   = queryLvl || (MODE === 'production' ? 'info' : 'debug');
log.setLevel(logLevel);                              // runtime-level

export const sessionId = (() => {
    const cached = localStorage.getItem('profocom_sid');
    if (cached) return cached;
    const sid = nanoid();
    localStorage.setItem('profocom_sid', sid);
    return sid;
})();

function envelope(level, message, meta = {}) {
    return {
        ts    : new Date().toISOString(),
        sid   : sessionId,
        path  : location.pathname,
        level,
        msg   : message,
        ...meta,
    };
}

function ship(entry) {
    const line = JSON.stringify(entry);
    (entry.level === 'error' ? console.error : console.log)(line);
}

['trace','debug','info','warn','error'].forEach(lvl => {
    const orig = log[lvl].bind(log);
    log[lvl] = (msg, meta) => {
        ship(envelope(lvl, msg, meta));
        orig(msg, meta);
    };
});


log.child = base => ({
    trace:(m,x)=>log.trace(m,{...base,...x}),
    debug:(m,x)=>log.debug(m,{...base,...x}),
    info :(m,x)=>log.info (m,{...base,...x}),
    warn :(m,x)=>log.warn (m,{...base,...x}),
    error:(m,x)=>log.error(m,{...base,...x}),
});

export default log;
