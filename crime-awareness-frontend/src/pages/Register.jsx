import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function Register() {

  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleRegister = async () => {

    try {

     await axios.post("http://localhost:8080/api/auth/register", {
        name: name,
        email: email,
        password: password
      });

      alert("Registration Successful");

      navigate("/login");

    } catch (err) {

      console.error(err);
      alert("Registration Failed");

    }

  };

  return (

    <div style={{ padding: "40px", textAlign: "center" }}>

      <h2>Register</h2>

      <input
        placeholder="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />

      <br /><br />

      <input
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />

      <br /><br />

      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />

      <br /><br />

      <button onClick={handleRegister}>
        Register
      </button>

    </div>

  );
}