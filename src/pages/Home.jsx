import { useEffect, useRef, useState } from "react";
import api   from "../api/gateway";
import log   from "../observability/logger.js";
import { inc, observe } from "../observability/metrics.js";

const STEP = 6;

export default function Home() {
    const [posts, setPosts] = useState([]);
    const [err,   setErr]   = useState(null);
    const [load,  setLoad]  = useState(false);

    const first = useRef(false);

    useEffect(() => {
        if (first.current) return;
        first.current = true;
        loadMore();
    }, []);

    const loadMore = () => {
        if (load) return;
        setLoad(true);
        inc('news_load_click_total');

        api.get('/api/v1/posts/simple', { params: { offset: posts.length, limit: STEP } })
            .then(part => {
                setPosts(prevPosts => {
                    const seen = new Set(prevPosts.map(p => p.url));
                    const filtered = part.filter(p => !seen.has(p.url));
                    return [...prevPosts, ...filtered];
                });
                observe('news_batch_size', part.length);
                inc('news_load_ok_total');
                log.info('News batch received', { count: part.length });
            })
            .catch(e => {
                setErr(e.message);
                inc('news_load_err_total');
                log.error('News batch error', { msg: e.message });
            })
            .finally(() => setLoad(false));
    };

    if (err) return <p style={{ color: 'red' }}>Ошибка: {err}</p>;

    return (
        <main>
            <h2 style={{ marginBottom: 24 }}>Новости профкома</h2>

            <div className="card-grid">
                {posts.map(p => (
                    <a
                        key={p.url}
                        href={p.url}
                        target="_blank"
                        rel="noreferrer"
                        className="card"
                        onClick={() => inc('news_open_total')}
                    >
                        <div className="card-img">
                            {p.photo && <img src={p.photo} alt="" />}
                        </div>
                        <div className="card-body">
                            <div className="card-title">{p.text}</div>
                        </div>
                        <div className="card-date">{p.date}</div>
                    </a>
                ))}
            </div>

            <div style={{ textAlign: 'center', marginTop: 32 }}>
                <button className="btn-primary" onClick={loadMore} disabled={load}>
                    {load ? 'Загрузка…' : 'Загрузить ещё'}
                </button>
            </div>
        </main>
    );
}
