import { useEffect, useState, useCallback } from "react";
import { createPortal } from "react-dom";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import ruLocale from "@fullcalendar/core/locales/ru";
import axios from "../api/gateway";
import api from "../api/gateway";
import { saveAs } from "file-saver";
import log from "../observability/logger.js";
import { inc } from "../observability/metrics.js";
import dayjs from "dayjs";
import duration from "dayjs/plugin/duration";
import "../styles/eventsCalendar.css";

dayjs.extend(duration);

function Modal({ open, onClose, children }) {
    if (!open) return null;

    return createPortal(
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-card" onClick={e => e.stopPropagation()}>
                <button className="modal-close" onClick={onClose} aria-label="Закрыть">✕</button>
                {children}
            </div>
        </div>,
        document.body
    );
}

function formatDuration(ms) {
    const d = dayjs.duration(ms);
    const parts = [];
    if (d.days()) parts.push(`${d.days()} д`);
    if (d.hours()) parts.push(`${d.hours()} ч`);
    if (d.minutes()) parts.push(`${d.minutes()} мин`);
    if (d.seconds() && !d.hours() && !d.days()) parts.push(`${d.seconds()} сек`);
    return parts.join(" ") || "0 мин";
}

export default function Events() {
    const [events, setEvents] = useState([]);
    const [err, setErr] = useState(null);
    const [selectedEvent, setSelectedEvent] = useState(null);

    const downloadIcs = useCallback(async () => {
        try {
            const res = await axios.get("/api/v1/events/ics", {
                responseType: "blob",
                transformResponse: x => x,
            });
            saveAs(res, "events.ics");
        } catch (e) {
            alert("Не удалось скачать календарь: " + e.message);
        }
    }, []);

    useEffect(() => {
        api.get("/api/v1/events")
            .then(list => {
                const ev = list.map(e => ({
                    id: e.id,
                    title: e.title,
                    start: new Date(e.startsAt * 1000),
                    end: new Date(e.endsAt * 1000),
                    extendedProps: {
                        desc: e.description,
                        loc: e.location,
                    },
                }));
                setEvents(ev);
                inc("events_fetch_ok_total", {}, ev.length);
                log.info("Events fetched", { count: ev.length });
            })
            .catch(e => {
                setErr(e.message);
                inc("events_fetch_err_total");
                log.error("Events fetch error", { msg: e.message });
            });
    }, []);

    if (err) return <p style={{ color: "red", textAlign: "center" }}>Ошибка: {err}</p>;

    return (
        <main>
            <FullCalendar
                plugins={[dayGridPlugin, interactionPlugin]}
                locale={ruLocale}
                initialView="dayGridMonth"
                height="auto"
                dayMaxEventRows={3}
                customButtons={{
                    exportIcs: {
                        text: "Экспорт",
                        click: downloadIcs,
                        classNames: ["fc-button"],
                    },
                }}
                headerToolbar={{
                    left: "prev,next today",
                    center: "title",
                    right: "exportIcs",
                }}
                events={events}
                eventClick={info => {
                    const { title, extendedProps, start, end } = info.event;
                    inc("event_click_total");
                    log.info("Event click", { id: info.event.id });
                    setSelectedEvent({
                        title,
                        desc: extendedProps.desc,
                        loc: extendedProps.loc,
                        startStr: dayjs(start).format("DD.MM.YYYY HH:mm"),
                        durStr: formatDuration(end - start),
                    });
                }}
            />

            <Modal open={!!selectedEvent} onClose={() => setSelectedEvent(null)}>
                {selectedEvent && (
                    <div className="modal-content">
                        <h2>{selectedEvent.title}</h2>
                        {selectedEvent.desc && <p style={{ whiteSpace: "pre-line" }}>{selectedEvent.desc}</p>}
                        <p><strong>Место:</strong> {selectedEvent.loc || "—"}</p>
                        <p><strong>Начало:</strong> {selectedEvent.startStr}</p>
                        <p><strong>Длительность:</strong> {selectedEvent.durStr}</p>
                    </div>
                )}
            </Modal>
        </main>
    );
}
