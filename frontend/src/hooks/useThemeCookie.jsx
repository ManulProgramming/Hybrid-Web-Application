import { useState, useEffect, useCallback } from "react";

function useThemeCookie(cookieName = "theme", defaultTheme = "dark") {
    const getCookie = (name) => {
        const match = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`));
        return match ? match[2] : null;
    };

    const setCookie = (name, value, days = 365) => {
        const expires = new Date(Date.now() + days * 864e5).toUTCString();
        document.cookie = `${name}=${value}; path=/; expires=${expires}`;
    };

    const getInitialTheme = () => {
        const cookieTheme = getCookie(cookieName);
        if (cookieTheme === "light" || cookieTheme === "dark") {
            return cookieTheme;
        }
        return defaultTheme;
    };

    const [theme, setThemeState] = useState(getInitialTheme);

    useEffect(() => {
        document.querySelector("body").dataset.bsTheme=theme;
    }, [theme]);

    const setTheme = useCallback(
        (newTheme) => {
            if (newTheme !== "light" && newTheme !== "dark") return;
            if (theme === newTheme) return;

            setThemeState(newTheme);
            document.querySelector("body").dataset.bsTheme=newTheme;
            setCookie(cookieName, newTheme);
        },
        [theme, cookieName]
    );

    const toggleTheme = useCallback(() => {
        setTheme(theme === "light" ? "dark" : "light");
    }, [theme, setTheme]);

    return { theme, setTheme, toggleTheme };
}

export default useThemeCookie;