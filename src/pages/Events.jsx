import { useEffect, useState } from "react";
import FullCalendar          from "@fullcalendar/react";
import dayGridPlugin         from "@fullcalendar/daygrid";
import interactionPlugin     from "@fullcalendar/interaction";
import ruLocale              from "@fullcalendar/core/locales/ru";

import api    from "../api/gateway";
import log    from "../observability/logger.js";
import { inc } from "../observability/metrics.js";

export default function Events(){
    const [events, setEvents] = useState([]);
    const [err, setErr]       = useState(null);

    useEffect(()=>{
        api.get('/api/v1/events')
            .then(list=>{
                const ev = list.map(e=>({
                    id   : e.id,
                    title: e.title,
                    start: e.startsAt,
                    end  : e.endsAt,
                    extendedProps:{ desc:e.description, loc:e.location }
                }));
                setEvents(ev);
                inc('events_fetch_ok_total', {}, ev.length);
                log.info('Events fetched', { count: ev.length });
            })
            .catch(e=>{
                setErr(e.message);
                inc('events_fetch_err_total');
                log.error('Events fetch error', { msg: e.message });
            });
    },[]);

    if(err) return <p style={{color:'red'}}>Ошибка: {err}</p>;

    return (
        <main>
            <h2 style={{textAlign:'center',marginTop:32}}>Календарь мероприятий</h2>

            <FullCalendar
                plugins={[dayGridPlugin, interactionPlugin]}
                locale={ruLocale}
                initialView="dayGridMonth"
                height="auto"
                dayMaxEventRows={3}
                headerToolbar={{ left:"prev,next today", center:"title", right:"" }}
                events={events}
                eventClick={info=>{
                    inc('event_click_total');
                    log.info('Event click', { id: info.event.id });
                    const {title, extendedProps} = info.event;
                    alert(`${title}\n\n${extendedProps.desc}\n\n📍 ${extendedProps.loc}`);
                }}
            />
        </main>
    );
}
