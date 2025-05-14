import { useEffect, useState } from "react";
import FullCalendar          from "@fullcalendar/react";
import dayGridPlugin         from "@fullcalendar/daygrid";
import interactionPlugin     from "@fullcalendar/interaction";
import ruLocale              from "@fullcalendar/core/locales/ru";
import api                   from "../api/gateway";
import "../styles/eventsCalendar.css";

export default function Events() {
    const [events, setEvents] = useState([]);
    const [error,  setError]  = useState(null);

    useEffect(() => {
        api.get("/api/v1/events")
            .then(data => {
                const ev = data.map(e => ({
                    id   : e.id,
                    title: e.title,
                    start: e.startsAt,
                    end  : e.endsAt,
                    extendedProps:{
                        desc: e.description,
                        loc : e.location
                    }
                }));
                setEvents(ev);
            })
            .catch(err => setError(err.message));
    }, []);

    if (error)
        return <p style={{color:"red",textAlign:"center"}}>Ошибка: {error}</p>;

    return (
        <main>
            <h2 style={{textAlign:"center",marginTop:32}}>
                Календарь мероприятий
            </h2>

            <FullCalendar
                plugins={[dayGridPlugin, interactionPlugin]}
                locale={ruLocale}
                initialView="dayGridMonth"
                height="auto"
                dayMaxEventRows={3}
                headerToolbar={{
                    left  : "prev,next today",
                    center: "title",
                    right : ""
                }}
                events={events}
                eventClick={info => {
                    const {title, extendedProps} = info.event;
                    alert(`${title}\n\n${extendedProps.desc}\n\n📍 ${extendedProps.loc}`);
                }}
            />
        </main>
    );
}
