import {useState, useEffect, useRef} from 'react';
import {useUserPost} from "../context/UserPostContext.jsx";
import {useVideoComments} from "../context/VideoCommentsContext.jsx";

function VideoComments() {
    const apiUrl = import.meta.env.VITE_API_URL;
    const [badImages, setBadImages] = useState(() => new Set());
    const { content, setContent, setP, setS } = useVideoComments();
    const { userId } = useUserPost();
    const [modal, setModal] = useState({
        status: false,
        header: "Are you sure?",
        body: "You are about to delete this comment.",
        onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
    });
    const modalRef = useRef(null);
    const handleImageError = (id) => {
        setBadImages(prev => {
            const next = new Set(prev);
            next.add(id);
            return next;
        });
    };
    /*useEffect(() => {
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
    }, [p,s]);*/

    useEffect(() => {
        if (modal.status){
            const mod = new bootstrap.Modal(modalRef.current);
            mod.show()
        }
    },[modal])

    async function deleteComment(id){
        try{
            const response = await fetch(apiUrl+'p/'+postId+'/c/'+id, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json"
                }
            });
            const data=await response.json();
            if (data.status.includes('200')){
                setContent(prev => ({
                    ...prev,
                    content: prev.content.filter(item => item.id !== id)
                }));
            }
        }catch(err){
            console.error("Error deleting comment: ", id);
        }
    }

    const removeComment = (id) => {
        setModal(prev => ({
            ...prev,
            ["status"]: true,
            ["onclick"]: async () => {
            setModal(prev => ({...prev, ["status"]: false})); await deleteComment(id);}
        }));
    }


    if (content && content.totalElements && content.totalElements!==0) {
        const pages = [];
        if (content.totalPages > 6) {
            const start =
                content.page < 4
                    ? 1
                    : content.page - 1 > content.totalPages - 4
                        ? content.totalPages - 4
                        : content.page - 1;

            const end =
                content.page >= content.totalPages - 2
                    ? content.totalPages
                    : 5 > content.page + 1
                        ? 5
                        : content.page + 1;

            for (let i = start; i <= end; i++) {
                pages.push(i);
            }
        }
        return (
            <>
                {userId && userId!==0 && currentUserId!==0 && (
                    <div className="modal fade" ref={modalRef} id="updateModal" tabIndex="-1"
                         aria-labelledby="updateModalLabel"
                         aria-hidden="true">
                        <div className="modal-dialog">
                            <div className="modal-content">
                                <div className="modal-header">
                                    <h1 className="modal-title fs-5" id="updateModalLabel">{modal.header}</h1>
                                    <button type="button" className="btn-close" data-bs-dismiss="modal"
                                            aria-label="Close"></button>
                                </div>
                                <div className="modal-body">
                                    <span>{modal.body}</span>
                                </div>
                                <div className="modal-footer">
                                    <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Close
                                    </button>
                                    <button type="button" className="btn btn-primary" data-bs-dismiss="modal"
                                            onClick={modal.onclick}>
                                        Continue
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
                <div className="d-flex justify-content-between align-items-center mb-3">

                    <div className="text-muted small">
                        Showing {1+content.size*(content.page-1)}-{content.size*content.page} of {content.totalElements} total results
                    </div>

                    <div id="sizeSortForm" className="d-flex align-items-center gap-2">

                        <label htmlFor="size" className="text-muted small mb-0">
                            Posts per page
                        </label>

                        <select name="s" id="size" className="form-select form-select-sm" style={{"width": "110px"}}
                                value={content.size}
                                onChange={(e) => {setS(e.target.value)}}>
                            <option value="8">8</option>
                            <option value="16">16</option>
                            <option value="32">32</option>
                            <option value="64">64</option>
                            {content.size!==8 && content.size!==16 && content.size!==32 && content.size!==64 && (
                                <option value={content.size}>{content.size}</option>
                            )}
                        </select>
                    </div>

                </div>
                <div className="card-body container">
                    {content.content && content.content.map((comment, index) => (
                            <div
                                className="video-info d-flex mt-2 text-break justify-content-between outline bg-body-tertiary rounded p-2 ps-3"
                                key={index}>
                                <div className="d-flex align-items-center">
                                    <a className="text-body"
                                       href={`/u/${comment.userId}`}>
                                        <img className="rounded-circle object-fit-cover text-body"
                                             style={{"width": "36px", "height": "36px"}}
                                             src={`/media/u/${comment.userId}`}
                                             onError={() => handleImageError(comment.id)}/>
                                        <i className="bi bi-person-circle d-none" style={{"fontSize": "36px"}}></i>
                                    </a>
                                    <div className="ms-3 d-flex flex-column">
                                        <a href={`/u/${comment.userId}`}
                                           className="text-decoration-none text-muted">
                                            <span>{comment.username}</span>
                                        </a>
                                        <span>{comment.comment}</span>
                                    </div>
                                </div>
                                {(comment.userId===currentUserId || (userId && userId!==0 && comment.userId===userId && currentUserId===userId)) && (

                                        <div className="d-flex flex-column align-items-center">
                                            <button className="btn" onClick={() => removeComment(comment.id)}>
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        </div>

                                )}

                            </div>
                        )
                    )}
                </div>

                <div id="paginationForm" className="mt-5">
                    <nav>
                        {content.totalPages <= 6 && (
                            <ul className="pagination justify-content-center">
                                <li className="page-item">
                                    <a className="page-link page-prev-next" data-total-pages={content.totalPages}
                                       data-next-page={content.page - 1} href="#" aria-label="Previous"
                                       onClick={(e) => {
                                           e.preventDefault();
                                           parseInt(e.currentTarget.dataset.nextPage) >= 1 && parseInt(e.currentTarget.dataset.nextPage) <= parseInt(e.currentTarget.dataset.totalPages) ? setP(e.currentTarget.dataset.nextPage) : null
                                       }}>
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </li>
                                {Array.from({length: content.totalPages}).map((_, i) => (
                                    <li className={`page-item ${i + 1 === content.page ? "active" : ""}`} key={i + 1}>
                                    <a className={`page-link page-num ${i+1 === content.page ? "active" : ""}`}
                                           href="#" data-value={i+1}
                                           onClick={(e) => {e.preventDefault();setP(e.currentTarget.dataset.value)}}>{i+1}</a>
                                    </li>
                                ))}
                                <li className="page-item">
                                    <a className="page-link page-prev-next" data-total-pages={content.totalPages}
                                       data-next-page={content.page+1} href="#" aria-label="Next"
                                       onClick={(e) => {e.preventDefault();parseInt(e.currentTarget.dataset.nextPage) >= 1 && parseInt(e.currentTarget.dataset.nextPage) <= parseInt(e.currentTarget.dataset.totalPages) ? setP(e.currentTarget.dataset.nextPage) : null}}>
                                        <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </li>
                            </ul>
                        )}
                        {content.totalPages>6 && (
                            <ul className="pagination justify-content-center">
                                <li className="page-item">
                                    <a className="page-link page-prev-next" data-total-pages={content.totalPages}
                                       data-next-page={content.page-1} href="#" aria-label="Previous"
                                       onClick={(e) => {e.preventDefault();parseInt(e.currentTarget.dataset.nextPage) >= 1 && parseInt(e.currentTarget.dataset.nextPage) <= parseInt(e.currentTarget.dataset.totalPages) ? setP(e.currentTarget.dataset.nextPage) : null}}>
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </li>
                                {content.page-1>=3 && (
                                    <>
                                        <li className="page-item">
                                            <a className="page-link page-num" href="#" data-value="1"
                                               onClick={(e) => {e.preventDefault();setP(e.currentTarget.dataset.value)}}>1</a>
                                        </li>
                                        <li className="page-item">
                                            <a className="page-link page-num spacer" href="#"
                                               onClick={(e) => {e.preventDefault();let pc = prompt('Navigate to page'); pc ? setP(pc) : null}}>...</a>
                                        </li>
                                    </>
                                )}

                                {pages.map((pa,i) => (
                                    <li className={`page-item ${pa===content.page ? "active" : ""}`} key={i}>
                                        <a className={`page-link page-num ${pa===content.page ? "active" : ""}`}
                                           href="#" data-value={pa} onClick={(e) => {e.preventDefault();setP(e.currentTarget.dataset.value)}}>{pa}</a>
                                    </li>
                                ))}

                                {content.totalPages-content.page>=3 && (
                                    <>
                                        <li className="page-item">
                                            <a className="page-link page-num spacer" href="#" onClick={(e) => {e.preventDefault();let pc = prompt('Navigate to page'); pc ? setP(pc) : null}}>...</a>
                                        </li>
                                        <li className="page-item">
                                            <a className="page-link page-num" href="#" data-value={content.totalPages}
                                               onClick={(e) => {e.preventDefault();setP(e.currentTarget.dataset.value)}}>{content.totalPages}</a>
                                        </li>
                                    </>
                                )}

                                <li className="page-item">
                                    <a className="page-link page-prev-next" data-total-pages={content.totalPages}
                                       data-next-page={content.page+1} href="#" aria-label="Next"
                                       onClick={(e) => {e.preventDefault();parseInt(e.currentTarget.dataset.nextPage) >= 1 && parseInt(e.currentTarget.dataset.nextPage) <= parseInt(e.currentTarget.dataset.totalPages) ? setP(e.currentTarget.dataset.nextPage) : null}}>
                                        <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </li>
                            </ul>
                        )}
                    </nav>
                </div>
            </>
        )
    }else {
        return (
            <div className="text-center">
                <p className="fs-2 fw-medium mt-4 text-muted">Comments not found</p>
            </div>
        )
    }
}
export default VideoComments;