import {useContext, useEffect, useMemo, useState} from "react";
import axios from "axios";

import {ThemeContext} from "../App";
import log from "../observability/logger.js";
import {inc, observe, setGauge} from "../observability/metrics.js";

import pdfIconDark from "../recources/document-outlined-dark/icons8-документ-50.svg";
import pdfIconWhite from "../recources/document-outlined-white/icons8-документ-50.svg";


const slog = log.child({page: "Statement"});

const FIELDS = [
    "lastName",
    "firstName",
    "middleName",
    "birthDate",
    "email",
    "phone",
    "groupOrPosition",
];

export default function Statement() {

    const {theme} = useContext(ThemeContext);
    const pdfIcon = theme === "dark" ? pdfIconWhite : pdfIconDark;


    const [type, setType] = useState(null);
    const [form, setForm] = useState(
        FIELDS.reduce((acc, f) => ({...acc, [f]: ""}), {})
    );
    const [err, setErr] = useState(null);
    const [loading, setLoading] = useState(false);


    useEffect(() => {
        inc("stmt_page_open_total");
        slog.info("Page opened");
    }, []);


    const bind = (key) => (e) => {
        setForm((f) => ({...f, [key]: e.target.value}));

        slog.debug("Field change", {field: key, len: e.target.value.length});
    };


    const isReady = useMemo(
        () => Boolean(type) && FIELDS.every((k) => form[k]),
        [type, form]
    );

    const handleSend = async (e) => {
        e.preventDefault();
        if (!isReady || loading) return;

        setLoading(true);
        setErr(null);

        const t0 = performance.now();
        inc("stmt_submit_total", {type});
        slog.info("Submit statement", {type});

        try {
            const res = await axios.post(
                "http://localhost:8080/api/v1/statements",
                {...form, type},
                {responseType: "arraybuffer", headers: {Accept: "application/pdf"}}
            );

            const duration = performance.now() - t0;
            const size = res.data.byteLength;
            observe("stmt_pdf_duration_ms", duration, {type});
            observe("stmt_pdf_bytes", size, {type});
            inc("stmt_submit_ok_total", {type});
            slog.info("PDF generated", {type, duration_ms: duration.toFixed(1), size});

            setGauge("stmt_last_duration_ms", duration, {type});

            const disposition = res.headers["content-disposition"] || "";
            const fname =
                /filename="?([^"]+)"?/.exec(disposition)?.[1] ||
                `${type.toLowerCase()}-${Date.now()}.pdf`;

            const blob = new Blob([res.data], {type: "application/pdf"});
            const url = URL.createObjectURL(blob);
            Object.assign(document.createElement("a"), {href: url, download: fname}).click();
            URL.revokeObjectURL(url);
        } catch (error) {
            inc("stmt_submit_err_total", {type});
            slog.error("PDF generation failed", {type, msg: error.message});
            setErr(error.message || "Не удалось скачать PDF");
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="stmt">
            <h2>Документы первичной профсоюзной организации</h2>

            <div className="doc-chooser">
                {[
                    {t: "TICKET", title: "Заявление о вступлении\nв Профсоюз"},
                    {t: "AID", title: "Заявление на материальную\nпомощь"},
                ].map(({t, title}) => (
                    <button
                        key={t}
                        onClick={() => {
                            setType(t);
                            inc("stmt_type_select_total", {type: t});
                            slog.debug("Type selected", {type: t});
                        }}
                        className={type === t ? "active" : ""}
                    >
                        <img src={pdfIcon} alt=""/>
                        <span style={{whiteSpace: "pre-line"}}>{title}</span>
                    </button>
                ))}
            </div>

            {type && (
                <form className="doc-form" onSubmit={handleSend}>
                    <h3>{type === "TICKET" ? "Вступление в Профсоюз" : "Материальная помощь"}</h3>

                    {FIELDS.map((f) => (
                        <input
                            key={f}
                            required
                            type={f === "birthDate" ? "date" : f === "email" ? "email" : "text"}
                            placeholder={
                                {
                                    lastName: "Фамилия",
                                    firstName: "Имя",
                                    middleName: "Отчество",
                                    birthDate: "Дата рождения",
                                    email: "E-mail",
                                    phone: "Телефон",
                                    groupOrPosition: "Группа / должность",
                                }[f]
                            }
                            value={form[f]}
                            onChange={bind(f)}
                        />
                    ))}

                    {err && <p className="err">{err}</p>}

                    <button className="btn-primary" disabled={loading || !isReady}>
                        {loading ? "Подготовка…" : "Скачать PDF"}
                    </button>
                </form>
            )}
        </main>
    );
}
