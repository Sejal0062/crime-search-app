import { Link } from "react-router-dom";
import "../App.css";

export default function Home() {
  return (
    <div className="landing-page">
      <header className="topbar">
        <Link to="/" className="brand"><span className="brand-mark">⚠</span><span>Crime<span className="brand-accent">Watch</span><small>AWARENESS PLATFORM</small></span></Link>
        <nav className="top-nav"><a href="#how-it-works">How it works</a><Link to="/dashboard" className="nav-cta">Explore reports <span>↗</span></Link></nav>
      </header>
      <main className="landing-hero">
        <div className="landing-copy">
          <div className="eyebrow"><span className="live-dot" /> INFORMATION. AWARENESS. ACTION.</div>
          <h1>Know what’s<br />happening <span>around you.</span></h1>
          <p>A clearer way to explore crime-related news and public awareness videos from across the web.</p>
          <div className="landing-actions"><Link to="/dashboard" className="primary-button">Explore reports <span>↗</span></Link><Link to="/dashboard" className="secondary-button">Browse India-wide news</Link></div>
          <div className="trust-line"><span>✓</span> Original publisher links &nbsp;·&nbsp; Search by crime and location</div>
        </div>
        <div className="landing-visual">
          <div className="visual-grid" />
          <div className="visual-ring" />
          <div className="visual-shield">⌑</div>
          <div className="visual-card visual-card-top"><span className="live-dot" /> PUBLIC REPORTS <b>Explore</b></div>
          <div className="visual-card visual-card-bottom"><span>⌕</span><div><b>Search with context</b><small>Crime type · Location · Sources</small></div></div>
          <div className="visual-label">AWARENESS STARTS WITH INFORMATION</div>
        </div>
      </main>
      <section className="landing-features" id="how-it-works">
        <div><span className="section-kicker">BUILT FOR AWARENESS</span><h2>Information, in one place.</h2></div>
        <div className="feature-row">
          <article><span>01</span><h3>Search reports</h3><p>Look for a specific crime keyword and optionally narrow by location.</p></article>
          <article><span>02</span><h3>Explore sources</h3><p>Read publisher summaries and open the original report for context.</p></article>
          <article><span>03</span><h3>Learn and stay aware</h3><p>Discover relevant public videos alongside news results.</p></article>
        </div>
      </section>
      <footer className="site-footer"><Link to="/" className="footer-brand">Crime<span>Watch</span></Link><p>Stay informed. Check sources. Put safety first.</p><span>PUBLIC INFORMATION • NOT AN EMERGENCY SERVICE</span></footer>
    </div>
  );
}
