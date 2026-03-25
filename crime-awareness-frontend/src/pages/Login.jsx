import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function Login() {

  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {

    try {

     await axios.post("http://localhost:8080/api/auth/login",  {
        email: email,
        password: password
      });

      alert("Login Successful");

      navigate("/dashboard");

    } catch (err) {

      console.error(err);
      alert("Login Failed");

    }

  };

  return (

    <div style={{ padding: "40px", textAlign: "center" }}>

      <h2>Login</h2>

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

      <button onClick={handleLogin}>
        Login
      </button>

    </div>

  );
}