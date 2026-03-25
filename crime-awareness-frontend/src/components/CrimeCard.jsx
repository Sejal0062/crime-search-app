function CrimeCard({ article }) {
  return (
    <div style={{ border: "1px solid #ccc", padding: "15px", margin: "10px" }}>
      <h3>{article.title}</h3>
      {article.imageUrl && <img src={article.imageUrl} width="100%" />}
      <p>{article.description}</p>
      <a href={article.url} target="_blank" rel="noreferrer">Read more</a>
    </div>
  );
}

export default CrimeCard;
