import {useState, useEffect, useRef} from 'react';
import { useUserPost } from '../context/UserPostContext';

function Video() {
    const apiUrl = import.meta.env.VITE_API_URL;
    const [formData, setFormData] = useState({
        id: 0,
        userId: 0,
        username: "",
        title: "",
        description: "",
        upvotes: 0,
        downvotes: 0,
        createdAt: 0
    });
    const [ratingData, setRatingData] = useState({})
    const [isAvaBad, setIsAvaBad] = useState(false);
    const [globalError, setGlobalError] = useState({});
    const [descriptionExpanded, setDescriptionExpanded] = useState(false);
    const { userId, setUserId } = useUserPost();
    const [errors, setErrors] = useState({});
    const [modal, setModal] = useState({
        status: false,
        header: "Are you sure?",
        body: "You are about to delete this post",
        onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
    });
    const modalRef = useRef(null);

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

    useEffect(() => {
        async function fetchVideo() {
            try {
                const response = await fetch(apiUrl + 'p/' + postId);
                const data = await response.json();
                if (data.status.includes('200') && data.content) {
                    setFormData(data.content);
                } else {
                    setGlobalError({"status": data.status, "msg": data.errors.error});
                }
            } catch (error) {
                setGlobalError({"status": "Error!", "msg": error.message});
            }
        }

        fetchVideo();
    }, [])

    useEffect(() => {
        setUserId(formData.userId);
    },[formData, setUserId])

    useEffect(() => {
        async function fetchVideoRating() {
            try {
                const response = await fetch(apiUrl + 'p/' + postId + '/r/');
                const data = await response.json();
                if (data.status.includes('200') && data.content) {
                    setRatingData(data.content);
                } else {
                    if (data.errors) {
                        setGlobalError({"status": data.status, "msg": data.errors.error});
                    }
                    setRatingData({});
                }
            } catch (error) {
                setGlobalError({"status": "Error!", "msg": error.message});
            }
        }
        fetchVideoRating();
    }, []);

    async function deletePost(){
        let modalHeader = '404';
        let modalBody = 'Unexpected error occurred.';
        try {
            const response = await fetch(apiUrl + 'p/' + postId, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                }
            });
            const data = await response.json();
            modalHeader = data.status;
            if (data.status.includes('401')){
                modalBody = "Unauthorized!";
                setModal({
                    status: true,
                    header: modalHeader,
                    body: modalBody,
                    onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
                });
            }else if (!data.status.includes('200')) {
                modalBody = "Unexpected error occurred.";
                setModal({
                    status: true,
                    header: modalHeader,
                    body: modalBody,
                    onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
                });
            }else{
                window.location.href = "/";
            }

        }catch(error){
            console.error("Error deleting post:", error);
        }
    }

    const handleDelete = () => {
        let modalBodyText= <>
            <b>You are about to delete this post!</b>
        </>;
        setModal({
            status: true,
            header: "Are you sure?",
            body: modalBodyText,
            onclick: async () => {setModal(prev => ({...prev, ["status"]: false})); await deletePost();}
        });
    }

    const validate = (name, value) => {
        let error = '';

        if (name === 'title'){
            if (!(/^.{1,100}$/.test(value))) {
                error = 'Unvalid title';
            }
        }
        if (name==="description"){
            if (!(/.{0,2000}/.test(value))) {
                error = 'Unvalid description';
            }
        }

        return error;
    };

    useEffect(() => {
        if (modal.status){
            const mod = new bootstrap.Modal(modalRef.current);
            mod.show()
        }
    },[modal])

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        setErrors(prev => ({
            ...prev,
            [name]: validate(name, value)
        }));
    };

    const handleSubmit = async () => {
        let should_we = false;
        if (/^.{1,100}$/.test(formData.title)) {
            if (/.{0,2000}/.test(formData.description)) {
                should_we = true;
            }else{
                setErrors(prev => ({
                    ...prev,
                    ["description"]: validate("description", formData.description)
                }));
            }
        }else{
            setErrors(prev => ({
                ...prev,
                ["title"]: validate("title", formData.title)
            }));
        }
        if (should_we) {
            try{
                const response = await fetch(apiUrl + 'p/' +postId, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(formData)
                });
                const data = await response.json();
                if (data.status.includes('200') && data.content) {
                    setErrors({});
                    setFormData(prev => ({
                        ...prev,
                        ["title"]: data.content.title,
                        ["description"]: data.content.description,
                    }));
                }else{
                    setErrors({
                        "title": "Unvalid title",
                        "description": "Unvalid description"
                    });
                }
            }catch(error) {
                console.error(error);
            }
        }
    }

    const changeRating = async (rating) => {
        if (currentUserId !== 0) {
            try {
                const res = await fetch(apiUrl + 'p/' + postId + '/r/', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({"rating": rating})
                });
                const data = await res.json();
                if (data.status.includes('200')) {
                    const tempRes = data.content;
                    if (!ratingData || ratingData === {} || ratingData.rating === null || ratingData.rating === undefined) {
                        if (tempRes && tempRes.rating === true) {
                            setFormData(prev => ({
                                ...prev,
                                ["upvotes"]: formData.upvotes + 1
                            }));
                        } else if (tempRes && tempRes.rating === false) {
                            setFormData(prev => ({
                                ...prev,
                                ["downvotes"]: formData.downvotes + 1
                            }));
                        }
                    } else if (ratingData.rating === true) {
                        if (tempRes && tempRes.rating === false) {
                            setFormData(prev => ({
                                ...prev,
                                ["upvotes"]: formData.upvotes - 1,
                                ["downvotes"]: formData.downvotes + 1
                            }));
                        } else if (!tempRes || tempRes === {} || tempRes.rating === null || tempRes.rating === undefined) {
                            setFormData(prev => ({
                                ...prev,
                                ["upvotes"]: formData.upvotes - 1
                            }));
                        }
                    } else if (ratingData.rating === false) {
                        if (tempRes && tempRes.rating === true) {
                            setFormData(prev => ({
                                ...prev,
                                ["upvotes"]: formData.upvotes + 1,
                                ["downvotes"]: formData.downvotes - 1
                            }));
                        } else if (!tempRes || tempRes === {} || tempRes.rating === null || tempRes.rating === undefined) {
                            setFormData(prev => ({
                                ...prev,
                                ["downvotes"]: formData.downvotes - 1
                            }));
                        }
                    }
                    setRatingData(data.content);
                } else {
                    window.location.href='/login';
                }
            } catch (err) {
                console.error(err);
            }
        } else {
            window.location.href = "/login";
        }
    }

    if (globalError && globalError.status) {
        return (
            <div className="m-5">
                <div className="text-center">
                    <h1 className="display-5 fw-bold">{globalError.status}</h1>
                    <p className="fs-2 fw-medium mt-4">Oops! {globalError.msg}</p>
                    <a href="/" className="btn btn-light fw-semibold rounded-pill px-4 py-2 custom-btn">
                        Go Home
                    </a>
                </div>
            </div>
        )
    }
    if (formData.userId !== 0) {
        return (
            <>
                {currentUserId===formData.userId && (
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
                <div className="fw-bold fs-5 lh-base text-break mt-2">
                {currentUserId===formData.userId
                    ?
                    <input className={`form-control ${errors.title ? "is-invalid" : ""}`} id="title" type="text"
                               name="title" maxLength="100"
                               placeholder="Enter Title..." required
                               value={formData.title} onChange={handleChange}/>
                    :
                    <span>{formData.title}</span>}
                </div>
                <div className="video-info mt-2 row d-flex">
                        <div className="col-auto">
                            <a className="text-body"
                               href={`/u/${formData.userId}`}>
                                <img
                                    className={`rounded-circle object-fit-cover text-body mt-2 ${isAvaBad ? "d-none" : ""}`}
                                    style={{"width": "50px", "height": "50px"}} src={`/media/u/${formData.userId}`}
                                    onError={() => setIsAvaBad(true)}/>
                                <i className={`bi bi-person-circle ${isAvaBad ? "" : "d-none"}`}
                                   style={{"fontSize": "50px"}}></i>
                            </a>
                        </div>
                        <div className="gap-2 col overflow-hidden text-truncate">
                            <a href={`/u/${formData.userId}`}
                               className="text-decoration-none text-body" style={{"fontSize": "1.1rem"}}>
                                <span>{formData.username}</span>
                            </a>

                            <div className="text-muted" style={{"fontSize": "0.7rem"}}>
                                <span>{formatDate(formData.createdAt)}</span>
                            </div>
                        </div>


                    <div className="col-auto" id="react-video-rating">
                        <div className="flex-row">
                            <button id="upvote" type="button"
                                    className={`btn ${ratingData && ratingData !== {} && ratingData.rating === true ? 'btn-outline-secondary' : ''}`}
                                    onClick={() => changeRating(true)}>
                                <i className={`bi bi-hand-thumbs-up${ratingData && ratingData !== {} && ratingData.rating ? '-fill' : ''}`}> </i><span>{formData.upvotes < 1000 ? formData.upvotes : Math.round(formData.upvotes / 1000.0 * 10.0) / 10.0 + 'K'}</span>
                            </button>
                            <button id="downvote" type="button"
                                    className={`btn ${ratingData && ratingData !== {} && ratingData.rating === false ? 'btn-outline-secondary' : ''}`}
                                    onClick={() => changeRating(false)}>
                                <i className={`bi bi-hand-thumbs-down${ratingData && ratingData !== {} && ratingData.rating === false ? '-fill' : ''}`}> </i><span>{formData.downvotes < 1000 ? formData.downvotes : Math.round(formData.downvotes / 1000.0 * 10.0) / 10.0 + 'K'}</span>
                            </button>
                        </div>
                        <div className="progress mt-2" style={{"height": "10px"}}>
                            <div className="progress-bar bg-success"
                                 style={{"width": +((formData.upvotes + formData.downvotes === 0) ? 0 : Math.round(100.0 * formData.upvotes / (formData.upvotes + formData.downvotes))) + '%'}}></div>
                            <div className="progress-bar bg-danger"
                                 style={{"width": +((formData.upvotes + formData.downvotes === 0) ? 0 : Math.round(100.0 * formData.downvotes / (formData.upvotes + formData.downvotes))) + '%'}}></div>
                        </div>
                    </div>
                </div>
                {currentUserId===formData.userId
                    ?
                    <div className="mt-3">
                    <textarea className={`form-control ${errors.description ? "is-invalid" : ""}`}
                    name="description" id="description" maxLength="2000" placeholder="Enter description..." rows="4"
                              required
                              value={formData.description}
                              onChange={handleChange}
                    ></textarea>
                        <div className="row mx-3 mt-3 gap-3 gap-md-5 justify-content-center">
                            <button className="col-8 col-md-2 btn btn-success" onClick={handleSubmit}><i
                                className="bi bi-floppy"></i> Save
                            </button>
                            <button className="col-8 col-md-2 btn btn-danger" onClick={handleDelete}><i
                                className="bi bi-trash3"></i> Delete
                            </button>
                        </div>
                    </div>
                    :
                    <>
                        {formData.description && formData.description.length > 0 && (
                            <div className={`description-wrapper mt-3 ${descriptionExpanded ? 'expanded' : ''}`}
                                 onClick={() => {
                                     setDescriptionExpanded(!descriptionExpanded);
                                 }}>
                                <p style={{"whiteSpace": "pre-line"}}>
                                    {formData.description}
                                </p>
                            </div>
                        )}
                    </>
                }
            </>
        )
    }
}

export default Video;