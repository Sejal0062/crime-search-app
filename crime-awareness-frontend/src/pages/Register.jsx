import { useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import "../App.css";

export default function Register() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRegister = async (event) => {
    event.preventDefault();
    setError("");
    if (password.length < 8) {
      setError("Use a password with at least 8 characters.");
      return;
    }
    setLoading(true);
    try {
      await axios.post("/api/auth/register", { name, email, password });
      navigate("/login", { state: { registered: true } });
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data || "Registration failed. This email may already be registered.");
    } finally {
      setLoading(false);
    }
  };

  return <div className="auth-page">
    <Link to="/" className="brand auth-brand"><span className="brand-mark">⚠</span><span>Crime<span className="brand-accent">Watch</span><small>AWARENESS PLATFORM</small></span></Link>
    <div className="auth-shell">
      <div className="auth-side"><span className="section-kicker">JOIN THE COMMUNITY</span><h1>Stay informed. Think critically. Share responsibly.</h1><p>Create an account to get started with your crime-awareness platform.</p><div className="auth-side-mark">CW<span> / 02</span></div></div>
      <form className="auth-card" onSubmit={handleRegister}>
        <span className="section-kicker">GET STARTED</span><h2>Create account</h2><p className="auth-subtitle">It only takes a minute.</p>
        <label>Full name<input value={name} onChange={e => setName(e.target.value)} placeholder="Your name" autoComplete="name" required /></label>
        <label>Email address<input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com" autoComplete="email" required /></label>
        <label>Password<input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="At least 8 characters" autoComplete="new-password" minLength={8} required /></label>
        {error && <p className="error-message" role="alert">{String(error)}</p>}
        <button className="primary-button auth-submit" type="submit" disabled={loading}>{loading ? "Creating account…" : "Create account"} <span>↗</span></button>
        <p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p>
        <Link to="/" className="back-link">← Back to home</Link>
      </form>
    </div>
  </div>;
}
