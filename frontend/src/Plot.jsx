import { useEffect, useState } from "react";

function Plot(){
    const [imgUrl, setImgUrl] = useState(null);

    useEffect(()=>{
        fetch("http://localhost:8000/plot", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify([1, 2, 3, 4]),
        })
            .then(res => res.blob())
            .then(blob => {
                const imgUrl = URL.createObjectURL(blob);
                setImgUrl(imgUrl);
            });
    },[]);

    return (
        <img src={imgUrl} alt="Plot" />
    )
}
export default Plot;