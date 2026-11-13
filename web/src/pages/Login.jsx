import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'

export default function Login() {
  const { login, isAuthenticated, sessionExpired } = useAuth()
  const [phoneNumber, setPhoneNumber] = useState('')
  const [pin, setPin] = useState('')
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
      setError(err.message) // includes "locked for 30 seconds" messages from the server
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page narrow">
      <h1>Log in</h1>
      {sessionExpired && <p className="error">Your session expired. Please log in again.</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Phone number
          <input
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            inputMode="numeric"
            maxLength={11}
            required
          />
        </label>
        <label>
          PIN
          <input
            type="password"
            value={pin}
            onChange={(e) => setPin(e.target.value)}
            inputMode="numeric"
            maxLength={6}
            required
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
      </form>
      <p>No account? <Link to="/register">Register</Link></p>
    </div>
  )
}
