export default function SearchBar({ onSearch }) {
  const [query, setQuery] = useState("");

  return (
  <div>
    <h1>Crime Awareness Platform</h1>

    {data && data.incidents && data.incidents.map((incident, i) => (
      <div className="incident-card" key={i}>
        <h3>{incident.article.title}</h3>
        <p>{incident.article.description}</p>

        {incident.videos && incident.videos.length > 0 && (
          <div className="video-row">
            {incident.videos.map((v, idx) => (
              <iframe
                key={idx}
                width="300"
                height="200"
                src={`https://www.youtube.com/embed/${v.url.split("v=")[1]}`}
                title="Crime video"
                allowFullScreen
              />
            ))}
          </div>
        )}
      </div>
    ))}
  </div>
);
}
