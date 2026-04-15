import {useState} from "react";

function Verify(){
    const apiUrl = import.meta.env.VITE_API_URL;
    const [code, setCode] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const validate = (value) => {
        let error = '';

        if (!(/^[0-9]{6}$/.test(value))) {
            error = 'Invalid code';
            setSuccess('');
        }
        return error;
    };

    const handleChange = (e) => {
        const { value } = e.target;

        setCode(value);

        setError(validate(value));
    };

    const handleClick = async () => {
        let username = document.getElementById("username");
        let usermail = document.getElementById("usermail");
        if (!(usermail) && username && username.value && /^([a-zA-Z0-9._-]{1,50}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})$/.test(username.value)){
            try {
                const res = await fetch(apiUrl+'code', {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({username: username.value})
                });
                const data = await res.json();
                if (data.status.includes('200')) {
                    setSuccess("Check your mail");
                    setError("");
                }else{
                    setError("This user does not exist");
                    setSuccess("");
                }
            } catch (err) {
                setError("This user does not exist");
                setSuccess("");
            }
        }else if (usermail && usermail.value && /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(usermail.value)){
            try {
                const res = await fetch(apiUrl+'code', {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({usermail: usermail.value})
                });
                const data = await res.json();

                if (data.status.includes('200')) {
                    setSuccess("Check your mail");
                    setError("");
                }else{
                    setError("This email is already in use");
                    setSuccess("");
                }
            } catch (err) {
                setError("This email is already in use");
                setSuccess("");
            }
        }else if (username){
            setError("Please enter a valid username");
            setSuccess("");
        }else{
            setError("Please enter a valid email address");
            setSuccess("");
        }
    }

    return (
        <>
            <label className="form-label" htmlFor="code">Code:</label>
            <div className="position-relative">
                <input type="text" id="code" name="code" className="form-control" maxLength="6"
                       placeholder="000000" value={code} onChange={handleChange} required/>
                <button type="button" className="bg-transparent border-0" id="send-code" onClick={handleClick}>
                    <i className="bi bi-envelope-arrow-up-fill"></i>
                </button>
            </div>
            {success=== "" && error === "" && (
                <div id="codeHelpBlock" className="form-text text-danger text-center">

                </div>
            )}
            {success=== "" && error !== "" && (
                <div className="form-text text-danger text-center">
                    {error}
                </div>
            )}
            {error === "" && success !== "" && (
                <div className="form-text text-center">
                    {success}
                </div>
            )}
        </>
    )
}

export default Verify;