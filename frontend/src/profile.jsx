import Profile from './components/Profile.jsx'
import ReactDOM from "react-dom/client";
import ProfileVideos from "./components/ProfileVideos.jsx";

const profileRoot = ReactDOM.createRoot(document.getElementById("react-profile"));
profileRoot.render(<Profile />);

const profileVideosRoot = ReactDOM.createRoot(document.getElementById("react-profile-videos"));
profileVideosRoot.render(<ProfileVideos />);