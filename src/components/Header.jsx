import {NavLink} from "react-router-dom";
import {useContext, useMemo} from "react";
import {ThemeContext} from "../App";

import "../styles/Header.css";

export default function Header() {
    const {toggleTheme} = useContext(ThemeContext);

    const links = useMemo(
        () => [
            {to: "/", label: "НОВОСТИ", exact: true},
            {to: "/events", label: "СОБЫТИЯ"},
            {to: "/about", label: "ГЛАВНАЯ"},
            {to: "/statement", label: "ДОКУМЕНТЫ ППО"},
            {to: "/faqs", label: "FAQ"},
        ],
        []
    );

    return (
        <header className="header">
            <div
                className="header-toggle"
                role="button"
                tabIndex={0}
                title="Сменить тему"
                onClick={toggleTheme}
                onKeyDown={e => e.key === "Enter" && toggleTheme()}
            >
                <div className="header-toggle-thumb"/>
            </div>

            <nav className="header-nav">
                {links.map(l => (
                    <NavLink key={l.to} to={l.to} end={l.exact}>
                        {l.label}
                    </NavLink>
                ))}
            </nav>
        </header>
    );
}
