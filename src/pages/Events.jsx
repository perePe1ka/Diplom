import {useEffect, useState} from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import ruLocale from "@fullcalendar/core/locales/ru";
import axios from "../api/gateway";
import api from "../api/gateway";
import {saveAs} from "file-saver";
import log from "../observability/logger.js";
import {inc} from "../observability/metrics.js";
import '../styles/eventsCalendar.css';



export default function Events() {
    const [events, setEvents] = useState([]);
    const [err, setErr] = useState(null);

    async function downloadIcs() {
        try {
            const res = await axios.get("/api/v1/events/ics", {
                responseType: "blob",
                transformResponse: x => x,
            });
            saveAs(res, "events.ics");
        } catch (e) {
            alert("Не удалось скачать календарь: " + e.message);
        }
    }

    useEffect(() => {
        api.get('/api/v1/events')
            .then(list => {
                const ev = list.map(e => ({
                    id: e.id,
                    title: e.title,
                    start: new Date(e.startsAt * 1000),
                    end: new Date(e.endsAt * 1000),
                    extendedProps: {desc: e.description, loc: e.location}
                }));
                setEvents(ev);
                inc('events_fetch_ok_total', {}, ev.length);
                log.info('Events fetched', {count: ev.length});
            })
            .catch(e => {
                setErr(e.message);
                inc('events_fetch_err_total');
                log.error('Events fetch error', {msg: e.message});
            });
    }, []);

    if (err) return <p style={{color: 'red', textAlign: 'center'}}>Ошибка: {err}</p>;

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
                        text: 'Экспорт',
                        click: downloadIcs,
                        classNames: ['fc-button']
                    }
                }}
                headerToolbar={{
                    left: "prev,next today",
                    center: "title",
                    right: "exportIcs"
                }}
                events={events}
                eventClick={info => {
                    inc('event_click_total');
                    log.info('Event click', {id: info.event.id});
                    const {title, extendedProps} = info.event;
                    alert(`${title}\n\n${extendedProps.desc}\n\n${extendedProps.loc}`);
                }}
            />
        </main>
    );
}
