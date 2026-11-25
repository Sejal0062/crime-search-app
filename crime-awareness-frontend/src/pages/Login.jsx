import { useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import "../App.css";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await axios.post("/api/auth/login", { email, password });
      navigate("/dashboard");
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data || "Login failed. Check your email and password.");
    } finally {
      setLoading(false);
    }
  };

  return <div className="auth-page">
    <Link to="/" className="brand auth-brand"><span className="brand-mark">⚠</span><span>Crime<span className="brand-accent">Watch</span><small>AWARENESS PLATFORM</small></span></Link>
    <div className="auth-shell">
      <div className="auth-side"><span className="section-kicker">YOUR AWARENESS SPACE</span><h1>Knowledge is a step toward safety.</h1><p>Sign in to continue exploring crime-related news and public information.</p><div className="auth-side-mark">CW<span> / 01</span></div></div>
      <form className="auth-card" onSubmit={handleLogin}>
        <span className="section-kicker">WELCOME BACK</span><h2>Sign in</h2><p className="auth-subtitle">Enter your account details below.</p>
        <label>Email address<input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com" autoComplete="email" required /></label>
        <label>Password<input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Your password" autoComplete="current-password" required /></label>
        {error && <p className="error-message" role="alert">{String(error)}</p>}
        <button className="primary-button auth-submit" type="submit" disabled={loading}>{loading ? "Signing in…" : "Sign in"} <span>↗</span></button>
        <p className="auth-switch">New to CrimeWatch? <Link to="/register">Create an account</Link></p>
        <Link to="/" className="back-link">← Back to home</Link>
      </form>
    </div>
  </div>;
}
