import {useState, useEffect, useRef } from 'react'

function Profile() {
    const apiUrl = import.meta.env.VITE_API_URL;
    const fileInputRef = useRef(null);
    const [isAvaBad, setIsAvaBad] = useState(false);
    const [formData, setFormData] = useState({
        username: '',
        usermail: '',
        userpass: '',
        oldUserpass: ''
    });
    const [avatarImg, setAvatarImg] = useState(`/media/u/${userId}`);
    const [errors, setErrors] = useState({});
    const [visibleOldUserpass, setVisibleOldUserpass] = useState(false);
    const [visibleUserpass, setVisibleUserpass] = useState(false);
    const [modal, setModal] = useState({
        status: false,
        header: "Are you sure?",
        body: "Please recheck your info:",
        onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
    });
    const modalRef = useRef(null);


    useEffect(() => {
        async function fetchUser() {
            try {
                const response = await fetch(apiUrl + 'u/' + userId);
                const data = await response.json();

                setFormData({
                    username: (data.content && data.content.username ? data.content.username : ""),
                    usermail: (data.content && data.content.usermail ? data.content.usermail : ""),
                    userpass: "",
                    oldUserpass: ""
                });
            } catch (error) {
                console.error("Error fetching user:", error);
            }
        }

        fetchUser();
    }, [])

    useEffect(() => {
        if (modal.status){
            const mod = new bootstrap.Modal(modalRef.current);
            mod.show()
        }
    },[modal])


    const handleClick = () => {
        fileInputRef.current.click();
    };

    const validate = (name, value) => {
        let error = '';

        if (name === 'username'){
            if (!(/^[a-zA-Z0-9._-]{1,50}$/.test(value))) {
                error = 'Login must only contain letters, digits, dash or underscore.';
            }
        }
        if (name === "usermail"){
            if (!(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value))) {
                error = "Email address needs to be valid.";
            }
        }
        if (name === "oldUserpass"){
            let feedbackMessage = [];
            const minLength = 8;
            const hasNumber = /\d/.test(value);
            const haslowercaseLetter = /[a-z]/.test(value);
            const hasuppercaseLetter = /[A-Z]/.test(value);
            const hasSpecial = /[@$!%*?&_-]/.test(value);
            const hasInvalid = /[^a-zA-Z0-9@$!%*?&_\S-]/.test(value);
            if (value.length < minLength) {
                feedbackMessage.push(`${minLength} Characters`);
            }
            if (!hasNumber) {
                feedbackMessage.push(`Numbers`);
            }
            if (!haslowercaseLetter) {
                feedbackMessage.push(`Lowercase letters`);
            }
            if (!hasuppercaseLetter) {
                feedbackMessage.push(`Uppercase letters`);
            }
            if (!hasSpecial) {
                feedbackMessage.push(`Special characters`);
            }
            if (hasInvalid) {
                feedbackMessage.push(`No invalid characters`);
            }
            if (!(/^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/.test(value))) {
                feedbackMessage.push(`Invalid password`);
            }
            error = feedbackMessage;
        }
        if (name === 'userpass'){
            if (value!=="" && value !== null){
                let feedbackMessage = [];
                const minLength = 8;
                const hasNumber = /\d/.test(value);
                const haslowercaseLetter = /[a-z]/.test(value);
                const hasuppercaseLetter = /[A-Z]/.test(value);
                const hasSpecial = /[@$!%*?&_-]/.test(value);
                const hasInvalid = /[^a-zA-Z0-9@$!%*?&_\S-]/.test(value);
                if (value.length < minLength) {
                    feedbackMessage.push(`${minLength} Characters`);
                }
                if (!hasNumber) {
                    feedbackMessage.push(`Numbers`);
                }
                if (!haslowercaseLetter) {
                    feedbackMessage.push(`Lowercase letters`);
                }
                if (!hasuppercaseLetter) {
                    feedbackMessage.push(`Uppercase letters`);
                }
                if (!hasSpecial) {
                    feedbackMessage.push(`Special characters`);
                }
                if (hasInvalid) {
                    feedbackMessage.push(`No invalid characters`);
                }
                if (!(/^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/.test(value))) {
                    feedbackMessage.push(`Invalid password`);
                }
                error = feedbackMessage;
            }
        }

        return error;
    };

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

    async function logoutUser(){
        try {
            const response = await fetch(apiUrl + 'logout/', {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                }
            });
            await response.json();
            window.location.href = "/login";
        } catch (error) {
            console.error("Error updating user:", error);
        }
    }

    async function deleteUser(){
        let modalHeader = '404';
        let modalBody = 'Unexpected error occurred.';
        try {
            const response = await fetch(apiUrl + 'u/' + userId, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(formData)
            });
            const data = await response.json();
            modalHeader = data.status;
            if (data.status.includes('401')){
                modalBody = "Unauthorized! Current password is incorrect.";
                setErrors({
                    username: "",
                    usermail: "",
                    oldUserpass: ["Password is incorrect"],
                    userpass: [],
                })
                setFormData({
                    username: (data.content && data.content.username ? data.content.username : formData.username),
                    usermail: (data.content && data.content.usermail ? data.content.usermail : formData.usermail),
                    userpass: "",
                    oldUserpass: ""
                });
                setVisibleOldUserpass(false);
                setVisibleUserpass(false);
                setModal({
                    status: true,
                    header: modalHeader,
                    body: modalBody,
                    onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
                });
            }else if (!data.status.includes('200')) {
                modalBody = "Unexpected error occurred.";
                setErrors({
                    username: "",
                    usermail: "",
                    oldUserpass: ["Password is incorrect"],
                    userpass: [],
                })
                setFormData({
                    username: (data.content && data.content.username ? data.content.username : formData.username),
                    usermail: (data.content && data.content.usermail ? data.content.usermail : formData.usermail),
                    userpass: "",
                    oldUserpass: ""
                });
                setVisibleOldUserpass(false);
                setVisibleUserpass(false);
                setModal({
                    status: true,
                    header: modalHeader,
                    body: modalBody,
                    onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
                });
            }else{
                window.location.href = "/register";
            }

        }catch(error){
            console.error("Error deleting user:", error);
        }
    }

    const handleFileChange = async (event) => {
        let modalHeader = 'Processing image';
        let modalBody = <>
            Profile picture is being processed.<br/>
        </>;
        const file = event.target.files[0];
        if (!file || !file.type.startsWith('image/')) return;

        const reader = new FileReader();
        reader.onload = e => {
            setAvatarImg(e.target.result);
        }
        reader.readAsDataURL(file);

        const tempData = new FormData();
        tempData.append("file", file);
        setModal({
            status: true,
            header: modalHeader,
            body: modalBody,
            onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
        });
        try {
            await fetch(apiUrl+'u/'+userId+'/a/', {
                method: "PATCH",
                body: tempData
            });
        } catch (err) {
            console.error(err);
        }
    };

    async function updateUser() {
        let modalHeader = '404';
        let modalBody = 'Unexpected error occurred.';
        try {
            const response = await fetch(apiUrl + 'u/' + userId, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(formData),
            });
            const data = await response.json();
            modalHeader = data.status;
            if (data.content) {
                modalBody = "Successfully updated!";
            }else if(data.status.includes('401')) {
                modalBody = "Unauthorized! Current password is incorrect.";
                setErrors({
                    username: "",
                    usermail: "",
                    oldUserpass: ["Password is incorrect"],
                    userpass: [],
                })
            }else if (data.status.includes('400')){
                modalBody = "That email already exists.";
                setErrors({
                    username: "",
                    usermail: "Email already exists.",
                    oldUserpass: "",
                    userpass: [],
                })
            }else {
                modalBody = "Unexpected error occurred.";
                setErrors({
                    username: "Login must only contain letters, digits, dash or underscore.",
                    usermail: "Email address needs to be valid.",
                    oldUserpass: ["Invalid password"],
                    userpass: ["Invalid password"],
                })
            }
            setFormData({
                username: (data.content && data.content.username ? data.content.username : formData.username),
                usermail: (data.content && data.content.usermail ? data.content.usermail : formData.usermail),
                userpass: "",
                oldUserpass: ""
            });
            setVisibleOldUserpass(false);
            setVisibleUserpass(false);
        } catch (error) {
            console.error("Error updating user:", error);
        }
        setModal({
            status: true,
            header: modalHeader,
            body: modalBody,
            onclick: (e) => {setModal(prev => ({...prev, ["status"]: false}))}
        });
    }

    const handleLogout = () => {
        let modalBodyText= <>
            You are about to logout from your account.
        </>;
        setModal({
            status: true,
            header: "Are you sure?",
            body: modalBodyText,
            onclick: async () => {setModal(prev => ({...prev, ["status"]: false})); await logoutUser();}
        });
    }

    const handleDelete = () => {
        let should_we = false;
        let modalBodyText= <>
            <b className="text-danger">You are about to delete your account and all the posts!</b>
        </>;
        if (/^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/.test(formData.oldUserpass)) {
            should_we = true;
        } else {
            setErrors(prev => ({
                ...prev,
                ["oldUserpass"]: validate("oldUserpass", formData.oldUserpass)
            }));
        }
        if (should_we){
            setModal({
                status: true,
                header: "Are you sure?",
                body: modalBodyText,
                onclick: async () => {setModal(prev => ({...prev, ["status"]: false})); await deleteUser();}
            });
        }
    }

    const handleSubmit = () => {
        let should_we = false;
        let modalBodyText= <>
            Please recheck your info:<br/>
            <b>Username</b>: {formData.username}<br/>
            <b>Email</b>: {formData.usermail}
        </>;

        if (formData.userpass === "" || formData.userpass === null || /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/.test(formData.userpass)) {
            if (/^[a-zA-Z0-9._-]{1,50}$/.test(formData.username)) {
                if (/^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/.test(formData.oldUserpass)) {
                    if (/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(formData.usermail)) {
                        should_we = true;
                    } else {
                        setErrors(prev => ({
                            ...prev,
                            ["usermail"]: validate("usermail", formData.usermail)
                        }));
                    }
                } else {
                    setErrors(prev => ({
                        ...prev,
                        ["oldUserpass"]: validate("oldUserpass", formData.oldUserpass)
                    }));
                }
            } else {
                setErrors(prev => ({
                    ...prev,
                    ["username"]: validate("username", formData.username)
                }));
            }
        } else {
            setErrors(prev => ({
                ...prev,
                ["userpass"]: validate("userpass", formData.userpass)
            }));
        }
        if (should_we){
            if (formData.userpass !== "" && formData.userpass !== null && formData.userpass) {
                modalBodyText= <>
                    Please recheck your info:<br/>
                    <b>Username</b>: {formData.username}<br/>
                    <b>Email</b>: {formData.usermail}<br/>
                    <u>You are also attempting to change your password!</u>
                </>
            }
            setModal({
                status: true,
                header: "Are you sure?",
                body: modalBodyText,
                onclick: async () => {setModal(prev => ({...prev, ["status"]: false})); await updateUser();}
            });
        }
    }

    if (userId===currentUserId) {
        return (
            <div className="card-body container">
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
                <div className="row g-4 align-items-center">
                    <div className="col-12 col-md-4 text-center">
                        <div className="profile-picture mt-2 mt-md-0">
                            <input type="file" name="file" style={{'display': 'none'}} ref={fileInputRef}
                                   onChange={handleFileChange} accept=".png, .jpg, .jpeg"/>
                            <div className="image-wrapper" onClick={handleClick}>
                                <img id="avatarImage" src={avatarImg}
                                     className={`${isAvaBad ? "d-none" : ""}`}
                                     onError={() => setIsAvaBad(true)}/>
                                <i className={`bi bi-person-circle ${isAvaBad ? "" : "d-none"}`}
                                   style={{'fontSize': '200px'}}></i>
                                <div className="overlay" style={isAvaBad ? {'inset': '57px 0 41px 0'} : {}}>
                                    <i className="bi bi-plus-lg"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="col-12 col-md-8 text-center" style={{'fontSize': '20px'}}>
                        <div className="mb-3 text-start">
                            <label className="form-label" htmlFor="username">Username:</label>
                            <input className={`form-control ${errors.username ? "is-invalid" : ""}`} type="text"
                                   name="username" maxLength="50"
                                   placeholder="Enter username..." value={formData.username} onChange={handleChange}
                                   required/>
                            {errors.username && (
                                <div className="form-text is-invalid">
                                    {errors.username}
                                </div>
                            )}
                        </div>
                        <div className="mb-3 text-start">
                            <label className="form-label" htmlFor="usermail">Email:</label>
                            <input className={`form-control ${errors.usermail ? "is-invalid" : ""}`} type="email"
                                   name="usermail" maxLength="89"
                                   placeholder="Enter email..." value={formData.usermail} onChange={handleChange}
                                   required/>
                            {errors.usermail && (
                                <div className="form-text is-invalid">
                                    {errors.usermail}
                                </div>
                            )}
                        </div>
                        <div className="mb-3 text-start">
                            <label className="form-label" htmlFor="oldUserpass">Current Password:</label>
                            <div className="position-relative">
                                <input type={visibleOldUserpass ? "text" : "password"} name="oldUserpass"
                                       className={`form-control ${errors.oldUserpass && errors.oldUserpass.length !== 0 ? "is-invalid" : ""}`}
                                       maxLength="150"
                                       placeholder="Enter password..." value={formData.oldUserpass}
                                       onChange={handleChange}
                                       required/>
                                <button type="button"
                                        className={`bg-transparent border-0 toggle-password ${errors.oldUserpass && errors.oldUserpass.length !== 0 ? 'me-3' : ""}`}
                                        onClick={() => setVisibleOldUserpass(!visibleOldUserpass)}>
                                    <i className={`bi bi-eye${visibleOldUserpass ? "" : "-slash"}`}></i>
                                </button>
                            </div>
                            {errors.oldUserpass && errors.oldUserpass.length !== 0 && (
                                <div className="form-text is-invalid text-danger">
                                    {errors.oldUserpass.map((errMsg, i) => (
                                        <ul className="my-0" key={i}><i className="bi bi-x-circle-fill"></i> {errMsg}
                                        </ul>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div className="mb-3 text-start">
                            <label className="form-label" htmlFor="userpass">New Password:</label>
                            <div className="position-relative">
                                <input type={visibleUserpass ? "text" : "password"} name="userpass"
                                       className={`form-control ${errors.userpass && errors.userpass.length !== 0 ? "is-invalid" : ""}`}
                                       maxLength="150"
                                       placeholder="Enter password..." value={formData.userpass} onChange={handleChange}
                                       required/>
                                <button type="button"
                                        className={`bg-transparent border-0 toggle-password ${errors.userpass && errors.userpass.length !== 0 ? 'me-3' : ""}`}
                                        onClick={() => setVisibleUserpass(!visibleUserpass)}>
                                    <i className={`bi bi-eye${visibleUserpass ? "" : "-slash"}`}></i>
                                </button>
                            </div>
                            {errors.userpass && errors.userpass.length !== 0 && (
                                <div className="form-text is-invalid text-danger">
                                    {errors.userpass.map((errMsg, i) => (
                                        <ul className="my-0" key={i}><i className="bi bi-x-circle-fill"></i> {errMsg}
                                        </ul>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div className="row mx-3 justify-content-center gap-3 gap-md-5">
                            <button className="col-12 col-md-3 btn btn-success" onClick={handleSubmit}><i
                                className="bi bi-floppy"></i> Save
                            </button>
                            <button className="col-12 col-md-3 btn btn-primary" onClick={handleLogout}><i
                                className="bi bi-box-arrow-right"></i> Logout
                            </button>
                            <button className="col-12 col-md-3 btn btn-danger" onClick={handleDelete}><i
                                className="bi bi-trash3"></i> Delete
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        )
    }else{
        return (
            <div className="card-body container">
                <div className="row g-4 align-items-center">
                    <div className="col-12 col-md-4 text-center">
                        <div className="profile-picture mt-2 mt-md-0">
                            <img id="avatarImage" src={`/media/u/${userId}`}
                                 className={`my-5 ${isAvaBad ? "d-none" : ""}`}
                                 onError={() => setIsAvaBad(true)}/>
                            <i className={`bi bi-person-circle ${isAvaBad ? "" : "d-none"}`}
                               style={{'fontSize': '200px'}}></i>
                        </div>
                    </div>
                    <div className="col-12 col-md-8 text-center" style={{'fontSize': '20px'}}>
                        <div className="mb-3 text-start">
                            <p className="form-label">Username:</p>
                            <p className="text-body-secondary">{formData.username}</p>
                        </div>
                        <div className="mb-3 text-start">
                            <p className="form-label">Email:</p>
                            <p className="text-body-secondary">{formData.usermail}</p>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default Profile;
