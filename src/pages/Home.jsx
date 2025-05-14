import { useEffect, useState, useRef } from "react";
import api from "../api/gateway";

const STEP = 6;

export default function Home() {
    const [posts, setPosts] = useState([]);
    const [err,   setErr]   = useState(null);
    const [loading, setLoad] = useState(false);

    const firstLoadDone = useRef(false);

    useEffect(() => {
        if (firstLoadDone.current) return;
        firstLoadDone.current = true;
        loadMore();
    }, []);

    const loadMore = () => {
        if (loading) return;
        setLoad(true);

        api.get("/api/v1/posts/simple", {
            params: { offset: posts.length, limit: STEP }
        })
            .then(newPart => setPosts(prev => [...prev, ...newPart]))
            .catch(e => setErr(e.message))
            .finally(() => setLoad(false));
    };

    if (err) return <p style={{color:"red"}}>Ошибка: {err}</p>;

    return (
        <main>
            <h2 style={{marginBottom:24}}>Новости профкома</h2>

            <div className="card-grid">
                {posts.map((p,i)=>(
                    <a key={i} href={p.url} target="_blank" rel="noreferrer" className="card">
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

            <div style={{textAlign:"center",marginTop:32}}>
                <button
                    className="btn-primary"
                    onClick={loadMore}
                    disabled={loading}
                >
                    {loading ? "Загрузка…" : "Загрузить ещё"}
                </button>
            </div>
        </main>
    );
}
