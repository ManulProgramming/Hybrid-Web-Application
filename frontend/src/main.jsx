import { createRoot } from "react-dom/client";
import ProfileApp from "./ProfileApp";
import Plot from "./Plot";

createRoot(
    document.getElementById("react-profile")
).render(<ProfileApp />);

createRoot(
    document.getElementById("react-plot")
).render(<Plot />);