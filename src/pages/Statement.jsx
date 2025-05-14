import {useContext, useMemo, useState} from "react";
import axios from "axios";

import {ThemeContext} from "../App";

import pdfIconDark from "../recources/document-outlined-dark/icons8-документ-50.svg";
import pdfIconWhite from "../recources/document-outlined-white/icons8-документ-50.svg";

export default function Statement() {
    const {theme} = useContext(ThemeContext);
    const pdfIcon = theme === "dark" ? pdfIconWhite : pdfIconDark;

    const [type, setType] = useState(null);
    const [form, setForm] = useState({
        lastName: "", firstName: "", middleName: "",
        birthDate: "", email: "", phone: "", groupOrPosition: ""
    });
    const [err, setErr] = useState(null);
    const [loading, setLd] = useState(false);

    const bind = key => e => setForm(f => ({...f, [key]: e.target.value}));

    const isReady = useMemo(
        () => type && Object.values(form).every(Boolean),
        [type, form]
    );

    const handleSend = async e => {
        e.preventDefault();
        if (!isReady) return;
        setLd(true);
        setErr(null);

        try {
            const res = await axios.post(
                "http://localhost:8080/api/v1/statements",
                {...form, type},
                {responseType: "arraybuffer", headers: {Accept: "application/pdf"}}
            );

            const blob = new Blob([res.data], {type: "application/pdf"});
            const cd = res.headers["content-disposition"] || "";
            const m = /filename="?([^"]+)"?/.exec(cd);
            const name = m ? m[1] : `${type.toLowerCase()}-${Date.now()}.pdf`;

            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = name;
            a.click();
            URL.revokeObjectURL(url);
        } catch (error) {
            setErr(error.message || "Не удалось скачать PDF");
        } finally {
            setLd(false);
        }
    };

    return (
        <main className="stmt">
            <h2>Документы первичной профсоюзной организации</h2>

            <div className="doc-chooser">
                <button
                    onClick={() => setType("TICKET")}
                    className={type === "TICKET" ? "active" : ""}
                >
                    <img src={pdfIcon} alt=""/>
                    <span>Заявление о вступлении<br/>в Профсоюз</span>
                </button>
                <button
                    onClick={() => setType("AID")}
                    className={type === "AID" ? "active" : ""}
                >
                    <img src={pdfIcon} alt=""/>
                    <span>Заявление на материальную<br/>помощь</span>
                </button>
            </div>

            {type && (
                <form className="doc-form" onSubmit={handleSend}>
                    <h3>{type === "TICKET" ? "Вступление в Профсоюз" : "Материальная помощь"}</h3>
                    <input
                        required
                        placeholder="Фамилия"
                        value={form.lastName}
                        onChange={bind("lastName")}
                    />
                    <input
                        required
                        placeholder="Имя"
                        value={form.firstName}
                        onChange={bind("firstName")}
                    />
                    <input
                        required
                        placeholder="Отчество"
                        value={form.middleName}
                        onChange={bind("middleName")}
                    />
                    <input
                        required
                        type="date"
                        value={form.birthDate}
                        onChange={bind("birthDate")}
                    />
                    <input
                        required
                        type="email"
                        placeholder="E-mail"
                        value={form.email}
                        onChange={bind("email")}
                    />
                    <input
                        required
                        placeholder="Телефон"
                        value={form.phone}
                        onChange={bind("phone")}
                    />
                    <input
                        required
                        placeholder="Группа / должность"
                        value={form.groupOrPosition}
                        onChange={bind("groupOrPosition")}
                    />

                    {err && <p className="err">{err}</p>}

                    <button className="btn-primary" disabled={loading || !isReady}>
                        {loading ? "Подготовка…" : "Скачать PDF"}
                    </button>
                </form>
            )}
        </main>
    );
}
