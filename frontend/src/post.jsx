import ReactDOM from "react-dom/client";
import Video from "./components/Video.jsx";
import {UserPostProvider} from "./context/UserPostContext.jsx";
import VideoComments from "./components/VideoComments.jsx";
import {VideoCommentsProvider} from "./context/VideoCommentsContext.jsx";
import VideoCommentNew from "./components/VideoCommentNew.jsx";

/*const postRoot = ReactDOM.createRoot(document.getElementById("react-video-info"));
postRoot.render(<UserPostProvider><Video /></UserPostProvider>);

const postCommentsRoot = ReactDOM.createRoot(document.getElementById("react-video-comments"));
postCommentsRoot.render(<UserPostProvider><VideoComments /></UserPostProvider>)*/


const postRoot = ReactDOM.createRoot(document.getElementById("react-video"));
postRoot.render(
    <UserPostProvider>
        <VideoCommentsProvider>
            <Video/>
            <hr className="mt-3 bg-body border-2 border-top"/>
            <p className="fs-3 fw-medium mt-4 text-muted">Comments</p>
            <VideoCommentNew />
            <VideoComments />
        </VideoCommentsProvider>
    </UserPostProvider>
);