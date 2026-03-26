import React, {createContext, useContext, useEffect, useState} from 'react';

const VideoCommentsContext = createContext(null);

export const useVideoComments = () => useContext(VideoCommentsContext);

export const VideoCommentsProvider = ({ children }) => {
    const apiUrl = import.meta.env.VITE_API_URL;
    const [content, setContent] = useState({});
    const [p, setP] = useState("1");
    const [s, setS] = useState("16");
    useEffect(() => {
        async function fetchVideoComments() {
            try {
                const response = await fetch(apiUrl + 'p/' + postId+`/c/?p=${encodeURIComponent(p)}&s=${encodeURIComponent(s)}`);
                const data = await response.json();
                setContent(data.content);
            } catch (error) {
                console.error("Error fetching user:", error);
            }
        }
        fetchVideoComments();
    }, [p,s]);

    return (
        <VideoCommentsContext.Provider value={{ content, setContent, setP, setS }}>
            {children}
        </VideoCommentsContext.Provider>
    );
};