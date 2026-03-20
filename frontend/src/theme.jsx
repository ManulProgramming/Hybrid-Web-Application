import { createRoot } from 'react-dom/client'
import ThemeSwitcher from "./components/ThemeSwitcher.jsx";

const containers = document.querySelectorAll(".react-theme");

containers.forEach(container => {
    const root = createRoot(container);
    root.render(<ThemeSwitcher />);
});