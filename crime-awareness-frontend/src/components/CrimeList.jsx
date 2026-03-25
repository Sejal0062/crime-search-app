import CrimeCard from "./CrimeCard";

function CrimeList({ crimes }) {

  if (!crimes || crimes.length === 0) {
    return <p>No crimes found</p>;
  }

  return (
    <div>
      {crimes.map((crime, i) => (
        <CrimeCard key={i} crime={crime} />
      ))}
    </div>
  );
}

export default CrimeList;
