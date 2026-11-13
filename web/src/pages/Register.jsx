import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'

export default function Register() {
  const [form, setForm] = useState({ fullName: '', phoneNumber: '', pin: '', confirmPin: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [created, setCreated] = useState(null)

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (!/^\d{11}$/.test(form.phoneNumber)) return setError('Phone number must be exactly 11 digits')
    if (!/^\d{4,6}$/.test(form.pin)) return setError('PIN must be 4 to 6 digits')
    if (form.pin !== form.confirmPin) return setError('PINs do not match')

    setBusy(true)
    try {
      const account = await api.register({
        fullName: form.fullName,
        phoneNumber: form.phoneNumber,
        pin: form.pin,
      })
      setCreated(account)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (created) {
    return (
      <div className="page narrow">
        <h1>Registration successful</h1>
        <p>Account number: <strong>{created.accountNumber}</strong></p>
        <p><Link to="/login">Go to log in</Link></p>
      </div>
    )
  }

  return (
    <div className="page narrow">
      <h1>Register</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Full name
          <input value={form.fullName} onChange={update('fullName')} maxLength={100} required />
        </label>
        <label>
          Phone number (11 digits)
          <input value={form.phoneNumber} onChange={update('phoneNumber')} inputMode="numeric" maxLength={11} required />
        </label>
        <label>
          PIN (4–6 digits)
          <input type="password" value={form.pin} onChange={update('pin')} inputMode="numeric" maxLength={6} required />
        </label>
        <label>
          Confirm PIN
          <input type="password" value={form.confirmPin} onChange={update('confirmPin')} inputMode="numeric" maxLength={6} required />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
      </form>
      <p>Already registered? <Link to="/login">Log in</Link></p>
    </div>
  )
}
