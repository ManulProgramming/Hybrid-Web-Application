import {useState, useEffect, useRef} from "react";

function ProfileVideo() {
    const apiUrl = import.meta.env.VITE_API_URL;
    const [content, setContent] = useState({});
    const [badImages, setBadImages] = useState(() => new Set());
    const [p, setP] = useState("1");
    const [s, setS] = useState("16");
    const [f, setF] = useState("hot");

    const handleImageError = (id) => {
        setBadImages(prev => {
            const next = new Set(prev);
            next.add(id);
            return next;
        });
    };
    useEffect(() => {
        async function fetchUserPosts() {
            try {
                const response = await fetch(apiUrl + 'u/' + userId+`/p/?p=${encodeURIComponent(p)}&s=${encodeURIComponent(s)}&f=${encodeURIComponent(f)}`);
                const data = await response.json();
                setContent(data.content);
            } catch (error) {
                console.error("Error fetching user:", error);
            }
        }

        fetchUserPosts();
    }, [p,s,f]);

    const formatDate = (ms) => {
        const date = new Date(ms);

        return date.toLocaleString("en-GB", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
        }).replace(",", "");
    };
    if (content && content.totalElements && content.totalElements!==0){
        const pages = [];
        if (content.totalPages>6){
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

                        <div className="dropdown">
                            <button className="btn" type="button" data-bs-toggle="dropdown">
                                <i className="bi bi-filter"></i>
                            </button>
                            <ul className="dropdown-menu">
                                <li><span style={{"padding": "0.25rem 1rem"}}>Sort</span></li>
                                <li>
                                    <hr className="dropdown-divider"/>
                                </li>
                                <li><a className="dropdown-item" href="#" data-value="hot" onClick={(e) => {e.preventDefault();setF(e.currentTarget.dataset.value)}}>Hot</a></li>
                                <li><a className="dropdown-item" href="#" data-value="latest" onClick={(e) => {e.preventDefault();setF(e.currentTarget.dataset.value)}}>Latest</a></li>
                            </ul>
                        </div>
                    </div>

                </div>
                <div className="row g-4">
                    {content.content && content.content.map((post, index) => (
                        <div className="col-lg-3 col-md-4 col-sm-6" key={index}>
                            <article className="video-card">

                                <a href={`/p/${post.id}`} className="thumbnail-link text-body">
                                    <img className="video-thumbnail"
                                         src={`/media/t/${post.id}`}
                                         onError={() => {this.src='/video_placeholder.jpg'}}/>
                                </a>

                                <div className="video-info d-flex row mt-2">
                                    <div className="col-auto">
                                        <a className="channel-avatar text-body"
                                           href={`/u/${post.userId}`}>
                                            <img src={`/media/u/${post.userId}`}
                                                 className={badImages.has(post.id) ? "d-none" : ""}
                                                 onError={() => handleImageError(post.id)}/>
                                            <i className={`bi bi-person-circle ${badImages.has(post.id) ? "" : "d-none"}`}
                                                style={{"fontSize": "36px"}}></i>
                                        </a>
                                    </div>

                                    <div className="col overflow-hidden text-truncate">

                                        <a href={`/p/${post.id}`}
                                           className="video-title text-decoration-none text-body">
                                            <span>{post.title}</span>
                                        </a>

                                        <a href={`/u/${post.userId}`}
                                           className="channel-name text-decoration-none text-muted">
                                            <span>{post.username}</span>
                                        </a>

                                        <div className="video-date text-muted">
                                    <span>{formatDate(post.createdAt)}</span>
                                        </div>

                                    </div>
                                </div>

                            </article>
                        </div>
                        )
                    )}
                </div>

                <div id="paginationForm" className="mt-5">
                    <nav>
                        {content.totalPages<=6 && (
                            <ul className="pagination justify-content-center">
                                <li className="page-item">
                                    <a className="page-link page-prev-next" data-total-pages={content.totalPages}
                                       data-next-page={content.page-1} href="#" aria-label="Previous"
                                       onClick={(e) => {e.preventDefault();parseInt(e.currentTarget.dataset.nextPage) >= 1 && parseInt(e.currentTarget.dataset.nextPage) <= parseInt(e.currentTarget.dataset.totalPages) ? setP(e.currentTarget.dataset.nextPage) : null}}>
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </li>
                                {Array.from({ length: content.totalPages }).map((_,i)=>(
                                    <li className={`page-item ${i+1===content.page ? "active" : ""}`} key={i+1}>
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
                <p className="fs-2 fw-medium mt-4 text-muted">Content not found</p>
            </div>
        )
    }
}

export default ProfileVideo;