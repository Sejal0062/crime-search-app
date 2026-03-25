import { useState } from "react";
import { searchCrime } from "../services/crimeApi";
import { Link } from "react-router-dom";

function Dashboard() {

  const [query,setQuery] = useState("");
  const [location,setLocation] = useState("");
  const [data,setData] = useState([]);

  const handleSearch = async () => {

    const result = await searchCrime(query,location);

    setData(result.incidents);

  };

  return (

    <div style={{
      maxWidth:"1000px",
      margin:"auto",
      padding:"20px",
      fontFamily:"Arial"
    }}>

      {/* NAVBAR */}

      <div style={{
        display:"flex",
        justifyContent:"space-between",
        marginBottom:"20px"
      }}>

        <h2>🚨 Crime Awareness System</h2>

        <div>
          <Link to="/" style={{marginRight:"15px"}}>Home</Link>
          <Link to="/login" style={{marginRight:"15px"}}>Login</Link>
          <Link to="/register">Register</Link>
        </div>

      </div>

      <h1 style={{textAlign:"center"}}>
        Crime Awareness Dashboard
      </h1>

      {/* SEARCH */}

      <div style={{textAlign:"center",marginBottom:"20px"}}>

        <input
          placeholder="Crime type (murder, theft...)"
          value={query}
          onChange={(e)=>setQuery(e.target.value)}
          style={{padding:"8px",marginRight:"10px"}}
        />

        <select
          value={location}
          onChange={(e)=>setLocation(e.target.value)}
          style={{padding:"8px",marginRight:"10px"}}
        >

          <option value="">Select City</option>
          <option value="mumbai">Mumbai</option>
          <option value="pune">Pune</option>
          <option value="bangalore">Bangalore</option>
          <option value="delhi">Delhi</option>

        </select>

        <button
          onClick={handleSearch}
          style={{
            padding:"8px 16px",
            background:"#007bff",
            color:"white",
            border:"none",
            borderRadius:"5px"
          }}
        >
          Search
        </button>

      </div>

      {/* RESULTS */}

      {data.map((crime,i)=>(

        <div
          key={i}
          style={{
            border:"1px solid #ddd",
            borderRadius:"10px",
            padding:"20px",
            marginBottom:"25px",
            boxShadow:"0 3px 10px rgba(0,0,0,0.1)"
          }}
        >

          <img
            src={crime.article?.imageUrl || "https://via.placeholder.com/800x400?text=Crime+News"}
            alt="crime"
            style={{
              width:"100%",
              borderRadius:"8px",
              marginBottom:"15px"
            }}
          />

          <h2>{crime.article?.title}</h2>

          <p>{crime.article?.description}</p>

          <p><b>Source:</b> {crime.article?.source}</p>

          <a href={crime.article?.url} target="_blank" rel="noreferrer">
            Read Full Article
          </a>

          <h4 style={{marginTop:"15px"}}>Related Videos</h4>

          {crime.videos?.slice(0,2).map((video,index)=>(

            <div key={index} style={{marginTop:"10px"}}>

              <img
                src={video.thumbnail}
                width="300"
                alt="video"
              />

              <p>{video.title}</p>

              <a href={video.url} target="_blank" rel="noreferrer">
                Watch Video
              </a>

            </div>

          ))}

        </div>

      ))}

    </div>

  );

}

export default Dashboard;