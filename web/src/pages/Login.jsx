
import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'
import '../styles/login.css'

export default function Login() {
  const { login, isAuthenticated, sessionExpired } = useAuth()

  const [phoneNumber, setPhoneNumber] = useState('')
  const [pin, setPin] = useState('')
  const [showPin, setShowPin] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (isAuthenticated) return <Navigate to="/" replace />

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)

    try {
      await login(phoneNumber, pin)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login-page">

      <div className="login-container">

  
        <div className="login-brand">
  <div className="login-logo-placeholder">
    <img src="/BB.png" alt="Bank of Banks Logo" />
  </div>

          <h1>Bank of Banks</h1>
          <p>Your money. Your control.</p>
        </div>

        {/* Login Card */}
        <div className="login-card">

          <div className="login-heading">
            <h2>Welcome Back</h2>
            <p>Sign in to access your banking account.</p>
          </div>

          {sessionExpired && (
            <div className="login-notice">
              Your session expired. Please log in again.
            </div>
          )}

          <form onSubmit={handleSubmit}>

            {/* Phone Number */}
            <label className="login-label">
              Phone Number

              <input
                type="tel"
                placeholder="Enter your 11-digit phone number"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                inputMode="numeric"
                maxLength={11}
                autoComplete="username"
                required
              />
            </label>

            {/* PIN */}
            <label className="login-label">
              PIN

              <div className="login-pin-wrapper">
                <input
                  type={showPin ? 'text' : 'password'}
                  placeholder="Enter your PIN"
                  value={pin}
                  onChange={(e) => setPin(e.target.value)}
                  inputMode="numeric"
                  maxLength={6}
                  autoComplete="current-password"
                  required
                />

                <button
                  type="button"
                  className="login-show-pin"
                  onClick={() => setShowPin(prev => !prev)}
                >
                  {showPin ? 'Hide' : 'Show'}
                </button>
              </div>
            </label>

            {/* Error */}
            {error && (
              <div className="login-error">
                {error}
              </div>
            )}

            {/* Submit */}
            <button
              type="submit"
              className="login-submit"
              disabled={busy}
            >
              {busy ? 'Logging in...' : 'Log In'}
            </button>

          </form>

          <div className="login-register">
            <p>
              Don't have an account?
              {' '}
              <Link to="/register">Create an account</Link>
            </p>
          </div>

        </div>

        {/* Footer */}
        <footer className="login-footer">
          <p>© 2026 Bank of Banks. All rights reserved.</p>
          <p>Secure Banking Experience</p>
        </footer>

      </div>
    </div>
  )
}
