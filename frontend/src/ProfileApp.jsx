import { useEffect, useState } from "react";

function ProfileApp(){
    const [id, setId] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [name, setName] = useState(null);

    useEffect(()=>{
        const fetchData = async () => {
            try {
                const response = await fetch('api/profile');
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const result = await response.json();
                setId(result.id);
                setName(result.name);
                setError(null);
            } catch (err) {
                setError(err.message);
                setId(0);
                setName("Anonymous");
            } finally {
                setIsLoading(false);
            }
        };
        fetchData();
    },[]);
    const handleSubmit = async (event) => {
        event.preventDefault();
        const data = { nickname: name };
            const response = await fetch('/api/profile', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            });

            const result = await response.json();
            let input = document.getElementById("changableUserName");
            if (result.success){
                console.log('Success:', result);
                input.classList.remove("is-invalid");
                input.classList.add("is-valid");
            }else{
                console.log("Failed");
                input.classList.add("is-invalid");
                input.classList.remove("is-valid");
            }

    };
    if (isLoading) {
        return <p>Loading data...</p>;
    }

    if (error) {
        return <p>Error: {error}</p>;
    }
    if (id===0){
        return (
            <div>
                <p>Id: {id}</p>
                <p>Username: {name}</p>
            </div>
        )
    }else{
    return (
        <div>
            <p>Id: {id}</p>
            <form id="changeUser" onSubmit={handleSubmit}>
                <label>Username:
                    <input class="form-control" type="text" name="changeUser" id="changableUserName" value={name} onChange={(e) =>{ setName(e.target.value); e.target.classList.remove("is-valid"); e.target.classList.remove("is-invalid")}} required/>
                </label><br/>
            </form>
        </div>
    );
    }
}
export default ProfileApp;