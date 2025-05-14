import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080",
});

api.interceptors.response.use((resp) => {
    const d = resp.data;
    if (Array.isArray(d)) return d;
    if (d && Array.isArray(d.content)) return d.content;
    return d;
});

export default api;
