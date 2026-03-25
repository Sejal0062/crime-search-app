import { Link } from "react-router-dom";

export default function Home() {

  return (

    <div style={{
      padding:"40px",
      textAlign:"center",
      fontFamily:"Arial"
    }}>

      <h1>🚨 Crime Awareness System</h1>

      <p>
        Stay informed about crime incidents around the world.
      </p>

      <br/>

      <Link to="/login">
        <button style={{padding:"10px 20px", marginRight:"10px"}}>
          Login
        </button>
      </Link>

      <Link to="/register">
        <button style={{padding:"10px 20px"}}>
          Register
        </button>
      </Link>

    </div>

  );
}