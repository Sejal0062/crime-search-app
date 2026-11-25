import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { searchCrime } from "../services/crimeApi";
import "../App.css";
import "../CrimeWatchAdvanced.css";

const categories = ["All reports", "Murder", "Fraud", "Cybercrime", "Robbery", "Theft", "Kidnapping", "Assault"];
const dateFilters = [{value:"all",label:"Any time"},{value:"24h",label:"Past 24 hours"},{value:"7d",label:"Past 7 days"},{value:"30d",label:"Past 30 days"}];
const clean = value => (value || "").trim();
const videoList = incidents => Array.from(new Map(incidents.flatMap(i => i.videos || []).filter(v => v.url && v.title).map(v => [v.url, v])).values()).slice(0, 8);

export default function Dashboard() {
  const [keyword, setKeyword] = useState("");
  const [location, setLocation] = useState("");
  const [category, setCategory] = useState("All reports");
  const [period, setPeriod] = useState("all");
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [activeSearch, setActiveSearch] = useState({query:"",location:""});
  const [lastUpdated, setLastUpdated] = useState(null);

  const load = useCallback(async (q = "", place = "") => {
    setLoading(true); setError(""); setActiveSearch({query:q,location:place});
    try {
      const data = await searchCrime(q, place);
      setIncidents(Array.isArray(data?.incidents) ? data.incidents : []);
      setLastUpdated(new Date());
    } catch (err) {
      setIncidents([]);
      setError(err?.response?.status === 429 ? "News provider limit reached. Please try again later." : "Could not load reports. Check that the backend is running and your news API is available.");
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);
  const submit = e => { e.preventDefault(); load(clean(keyword) || (category === "All reports" ? "" : category), clean(location)); };
  const selectCategory = item => { setCategory(item); setKeyword(""); load(item === "All reports" ? "" : item, clean(location)); };
  const visible = useMemo(() => {
    if (period === "all") return incidents;
    const days = period === "24h" ? 1 : period === "7d" ? 7 : 30;
    const cutoff = Date.now() - days * 86400000;
    return incidents.filter(item => {
      const stamp = item.article?.publishedAt;
      if (!stamp) return false;
      const date = new Date(stamp).getTime();
      return Number.isFinite(date) && date >= cutoff && date <= Date.now() + 86400000;
    });
  }, [incidents, period]);
  const videos = useMemo(() => videoList(visible), [visible]);
  const formatDate = value => { if (!value) return "Date unavailable"; const d = new Date(value); return Number.isNaN(d.getTime()) ? "Date unavailable" : d.toLocaleDateString("en-IN",{day:"numeric",month:"short",year:"numeric"}); };

  return <div className="cw-app">
    <header className="cw-header"><div className="cw-shell cw-header-inner">
      <Link to="/" className="cw-logo"><span className="cw-logo-icon">◈</span><span>Crime<span>Watch</span><small>INDIA • PUBLIC AWARENESS</small></span></Link>
      <nav className="cw-nav"><a href="#reports">Explore reports</a><a href="#videos">Video coverage</a><span className="cw-public">● Public access</span></nav>
    </div></header>
    <main className="cw-shell">
      <section className="cw-intro"><div><div className="cw-eyebrow"><span className="cw-dot"/> INDIA-WIDE NEWS DISCOVERY</div><h1>Stay informed.<br/><em>Understand what matters.</em></h1><p>Explore crime-related reporting across India, narrow your search by location, and read stories from their original publishers.</p></div><div className="cw-intro-aside"><span className="cw-aside-label">YOUR NEWS DESK</span><strong>Across India</strong><span>Public reports • News & videos</span><div className="cw-aside-line"/><small>Information for awareness, not an emergency response service.</small></div></section>
      <section className="cw-search-box" aria-label="Search news"><form onSubmit={submit}><label className="cw-keyword"><span>⌕</span><input aria-label="Crime keyword" value={keyword} onChange={e=>setKeyword(e.target.value)} placeholder="Search crime, case, or keyword…"/></label><label className="cw-place"><span>⌖</span><input aria-label="City or state" value={location} onChange={e=>setLocation(e.target.value)} placeholder="All India — optional city or state"/></label><button type="submit" disabled={loading}>{loading ? "Loading…" : "Search reports"} <span>↗</span></button></form><div className="cw-search-caption">Search is optional. The latest India-wide reports load automatically.</div></section>
      <section className="cw-category-section"><div className="cw-section-top"><span className="cw-label">BROWSE BY CATEGORY</span><span className="cw-hint">Select a topic to update results</span></div><div className="cw-chips">{categories.map(item=><button key={item} type="button" className={category===item?"active":""} onClick={()=>selectCategory(item)}>{item}</button>)}</div></section>
      <section id="reports" className="cw-reports"><div className="cw-results-head"><div><div className="cw-label">LATEST COVERAGE</div><h2>{activeSearch.query ? `Reports: ${activeSearch.query}` : "Crime news across India"}</h2><p>{activeSearch.location ? `Location filter: ${activeSearch.location}` : "Reports from public news sources across India"}</p></div><div className="cw-results-tools"><label>Published <select value={period} onChange={e=>setPeriod(e.target.value)}>{dateFilters.map(d=><option key={d.value} value={d.value}>{d.label}</option>)}</select></label><button type="button" onClick={()=>load(activeSearch.query,activeSearch.location)} disabled={loading}>↻ Refresh</button></div></div>
      <div className="cw-status-row"><span>{loading ? "Searching…" : `${visible.length} ${visible.length===1?"report":"reports"}`}</span><span>{lastUpdated ? `Updated ${lastUpdated.toLocaleTimeString("en-IN",{hour:"numeric",minute:"2-digit"})}` : "Waiting for updates"}</span></div>
      {error && <div className="cw-message cw-error" role="alert"><strong>Unable to fetch reports</strong><p>{error}</p><button onClick={()=>load(activeSearch.query,activeSearch.location)}>Try again</button></div>}
      {loading && <div className="cw-message"><div className="cw-spinner"/><strong>Gathering recent coverage…</strong><p>This may take a few moments.</p></div>}
      {!loading && !error && !visible.length && <div className="cw-message"><strong>No matching reports found</strong><p>{period!=="all" ? "No dated articles matched this time period. Try ‘Any time’ or a broader search." : "Try another keyword or location. News availability depends on the provider."}</p><button onClick={()=>{setPeriod("all");setCategory("All reports");setKeyword("");setLocation("");load();}}>Show all India</button></div>}
      {!loading && visible.length>0 && <div className="cw-article-grid">{visible.map((item,index)=>{const a=item.article||{};return <article className="cw-article" key={a.url||index}><div className="cw-article-image">{a.imageUrl?<img src={a.imageUrl} alt="" loading="lazy" onError={e=>{e.currentTarget.style.display="none";}}/>:null}<span className="cw-image-fallback">CW / NEWS</span></div><div className="cw-article-body"><div className="cw-article-meta"><span>NEWS REPORT</span><span>{formatDate(a.publishedAt)}</span></div><h3>{a.title||"Untitled report"}</h3><p>{a.description||"Read the original publisher's article for more context."}</p><div className="cw-article-bottom"><span>{a.source||"Publisher unavailable"}</span>{a.url&&<a href={a.url} target="_blank" rel="noopener noreferrer">Read source ↗</a>}</div></div></article>})}</div>}
      </section>
      <section id="videos" className="cw-videos"><div className="cw-results-head"><div><div className="cw-label">WATCH & LEARN</div><h2>Related video coverage</h2><p>Public videos associated with the current news search.</p></div></div>{!loading && videos.length>0?<div className="cw-video-grid">{videos.map((v,i)=><a key={v.url||i} className="cw-video" href={v.url} target="_blank" rel="noopener noreferrer"><div className="cw-video-thumb">{v.thumbnail&&<img src={v.thumbnail} alt="" loading="lazy"/>}<span>▶</span></div><div><strong>{v.title}</strong><small>Watch on YouTube ↗</small></div></a>)}</div>:<p className="cw-video-empty">{loading?"Loading related videos…":"No related videos available for this search."}</p>}</section>
      <div className="cw-disclaimer">CrimeWatch aggregates third-party reporting. Articles are not independently verified by CrimeWatch. Check original sources; do not use this website for emergency assistance.</div>
    </main><footer className="cw-footer"><div className="cw-shell"><strong>Crime<span>Watch</span></strong><span>Stay informed. Check sources. Put safety first.</span><small>PUBLIC INFORMATION • NOT AN EMERGENCY SERVICE</small></div></footer>
  </div>;
}
