import React from "react";
import useThemeCookie from "../hooks/useThemeCookie";

export default function ThemeSwitcher() {
  const { theme, toggleTheme } = useThemeCookie();
  return (
      <button className="btn btn-sm" onClick={toggleTheme}>
          <i className={`bi bi-${theme === 'light' ? 'moon' : 'sun'}`}></i>
      </button>
  );
}