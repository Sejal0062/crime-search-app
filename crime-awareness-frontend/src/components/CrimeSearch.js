import { useState } from "react";

function CrimeSearch() {
  const [query, setQuery] = useState("");
  const [location, setLocation] = useState("");
  const [results, setResults] = useState(null);

  const searchCrime = async () => {
    const response = await fetch(
      `http://localhost:8080/api/crime/search?query=${query}&location=${location}`
    );
    const data = await response.json();
    setResults(data);
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2>Crime Search</h2>

      {/* Crime keyword */}
      <input
        type="text"
        placeholder="Enter crime (e.g. robbery)"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      {/* Location */}
      <select
        value={location}
        onChange={(e) => setLocation(e.target.value)}
      >
        <option value="">Select Location</option>
        <option value="Mumbai">Mumbai</option>
        <option value="Pune">Pune</option>
        <option value="Delhi">Delhi</option>
        <option value="Bangalore">Bangalore</option>
      </select>

      <br /><br />

      <button onClick={searchCrime}>Search</button>

      {/* Output */}
      {results && (
        <pre>{JSON.stringify(results, null, 2)}</pre>
      )}
    </div>
  );
}

export default CrimeSearch;
