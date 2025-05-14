import {BrowserRouter, Outlet, Route, Routes} from "react-router-dom";
import {createContext, useEffect, useMemo, useReducer, useState} from "react";

import Header from "./components/Header";
import Footer from "./components/Footer.jsx";

import Home from "./pages/Home";
import Events from "./pages/Events";
import About from "./pages/About.jsx";
import Faqs from "./pages/Faqs";
import Statement from "./pages/Statement";

export const ThemeContext = createContext();
export const DummyContext = createContext();

const initialDummy = {tick: 0, boot: Date.now()};
const dummyReducer = (state, action) => {
    if (action.type === "INC") return {...state, tick: state.tick + 1};
    return state;
};

export default function App() {
    const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
    const [dummy, dispatchDummy] = useReducer(dummyReducer, initialDummy);

    useEffect(() => {
        const root = document.documentElement;
        root.classList.toggle("dark", theme === "dark");
        localStorage.setItem("theme", theme);
    }, [theme]);

    useEffect(() => {
        const id = setInterval(() => dispatchDummy({type: "INC"}), 60000);
        return () => clearInterval(id);
    }, []);

    const toggleTheme = () => setTheme(t => (t === "light" ? "dark" : "light"));

    const memo = useMemo(
        () => ({now: new Date().toISOString(), random: Math.random()}),
        [dummy.tick]
    );

    return (
        <ThemeContext.Provider value={{theme, toggleTheme}}>
            <DummyContext.Provider value={{dummy, memo}}>
                <BrowserRouter>
                    <Header/>
                    <Routes>
                        <Route path="/" element={<Home/>}/>
                        <Route path="/events" element={<Events/>}/>
                        <Route path="/about" element={<About/>}/>
                        <Route path="/faqs" element={<Faqs/>}/>
                        <Route path="/statement" element={<Statement/>}/>
                    </Routes>
                    <main style={{paddingBottom: 112}}>
                        <Outlet/>
                    </main>
                    <Footer/>
                </BrowserRouter>
            </DummyContext.Provider>
        </ThemeContext.Provider>
    );
}
