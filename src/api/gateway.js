import axios from 'axios';
import log from '../observability/logger.js';
import { incCounter, observeHistogram } from '../observability/metrics.js';

const api = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080',
    timeout: 100_000,
});

api.interceptors.request.use((config) => {
    config.meta = { start: performance.now() };
    incCounter('http_requests_total', {
        method: config.method,
        endpoint: config.url,
    });
    return config;
});

api.interceptors.response.use(
    (response) => {
        const dur = performance.now() - response.config.meta.start;
        observeHistogram('http_request_duration_ms', dur, {
            method: response.config.method,
            endpoint: response.config.url,
            status: response.status,
        });
        log.info(`HTTP ${response.status} ${response.config.url}`, {
            duration_ms: dur.toFixed(1),
        });
        return response.data;
    },
    (error) => {
        const dur =
            error.config?.meta ? performance.now() - error.config.meta.start : 0;
        incCounter('http_errors_total', {
            method: error.config?.method,
            endpoint: error.config?.url,
            status: error.response?.status || 'NO_RESP',
        });
        log.error(`HTTP ERR ${error.config?.url}`, {
            duration_ms: dur.toFixed(1),
            message: error.message,
        });
        return Promise.reject(error);
    }
);

export default api;
