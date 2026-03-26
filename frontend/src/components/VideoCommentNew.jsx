import {useState, useEffect, useRef} from 'react';
import {useVideoComments} from "../context/VideoCommentsContext.jsx";

function VideoCommentNew() {
    const apiUrl = import.meta.env.VITE_API_URL;
    const { setContent } = useVideoComments();
    const [commentInput, setCommentInput] = useState("");
    const [error, setError] = useState(false);
    const createComment = async () => {
        if (!commentInput) {
            setError(true);
        }else {
            try {
                const res = await fetch(apiUrl + 'p/' + postId + '/c/', {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({"comment": commentInput})
                });
                const data = await res.json();
                if (data.status.includes('200')) {
                    setContent(prev => ({
                        ...prev,
                        content: [
                            ...prev.content,
                            data.content
                        ]
                    }));
                }
            } catch (err) {
                console.error(err);
            }
            setCommentInput('');
        }
    }

    const handleChange = (e) => {
        const value = e.target.value;

        setCommentInput(value);
        if (value) {
            setError(false)
        }else{
            setError(true)
        }
    };

    if (currentUserId && currentUserId!==0) {
        return (
            <div className="d-flex gap-2 p-2 bg-body-tertiary rounded mb-3">
                <input className={`form-control ${error ? "is-invalid" : ""}`} id="comment" type="text"
                       name="comment" maxLength="100"
                       placeholder="Add a comment..." value={commentInput}
                       onChange={handleChange}
                       required/>
                <button className="btn btn-primary" onClick={createComment}>Comment</button>
            </div>
        )
    }else{
        return (<></>)
    }
}
export default VideoCommentNew;