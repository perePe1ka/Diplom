import { useEffect, useState } from "react";
import api                      from "../api/gateway";

export default function Faqs() {
    const [faqs, setFaqs]   = useState([]);
    const [err,  setErr]    = useState(null);
    const [open, setOpen]   = useState({});

    useEffect(() => {
        api.get("/api/v1/faqs")
            .then(setFaqs)
            .catch(e => setErr(e.message));
    }, []);

    const toggle = id => setOpen(o => ({ ...o, [id]: !o[id] }));

    if (err) return <p style={{ color: "red" }}>Ошибка: {err}</p>;

    return (
        <main>
            <h2 style={{ marginBottom: 24 }}>FAQ</h2>

            <ul style={{ display: "grid", gap: 16 }}>
                {faqs.map(f => (
                    <li
                        key={f.id}
                        onClick={() => toggle(f.id)}
                        style={{
                            background: "var(--card-bg)",
                            padding: 16,
                            borderRadius: 8,
                            boxShadow: "0 2px 6px #0001",
                            cursor: "pointer"
                        }}
                    >
                        <b>{f.question}</b>
                        <div
                            style={{
                                maxHeight: open[f.id] ? 500 : 0,
                                overflow: "hidden",
                                transition: "max-height .25s ease",
                                marginTop: open[f.id] ? 8 : 0
                            }}
                        >
                            {open[f.id] && <p style={{ margin: 0 }}>{f.answer}</p>}
                        </div>
                    </li>
                ))}
            </ul>
        </main>
    );
}
