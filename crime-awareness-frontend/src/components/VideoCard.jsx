export default function VideoCard({ video }) {
  return (
    <div className="video-card">
      <img src={video.thumbnailUrl} alt="thumbnail" />
      <h4>{video.title}</h4>
      <a href={video.videoUrl} target="_blank" rel="noreferrer">
        Watch
      </a>
    </div>
  );
}
