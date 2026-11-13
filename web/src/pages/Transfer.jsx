import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { formatMoney, parseAmount } from '../format.js'

// Steps: "lookup" (enter phone) -> "details" (confirm name, enter amount) -> "done"
export default function Transfer() {
  const [step, setStep] = useState('lookup')
  const [phone, setPhone] = useState('')
  const [recipient, setRecipient] = useState(null)
  const [amountText, setAmountText] = useState('')
  const [description, setDescription] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleLookup(e) {
    e.preventDefault()
    setError('')
    if (!/^\d{11}$/.test(phone)) return setError('Phone number must be exactly 11 digits')
    setBusy(true)
    try {
      setRecipient(await api.recipient(phone))
      setStep('details')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleSend(e) {
    e.preventDefault()
    setError('')
    const amount = parseAmount(amountText)
    if (amount === null) return setError('Enter a valid amount (up to 2 decimal places)')
    setBusy(true)
    try {
      const res = await api.transfer({
        recipientPhoneNumber: phone,
        amount,
        description: description.trim() || undefined,
      })
      setResult(res)
      setStep('done')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function reset() {
    setStep('lookup')
    setPhone('')
    setRecipient(null)
    setAmountText('')
    setDescription('')
    setResult(null)
    setError('')
  }

  if (step === 'done') {
    return (
      <div className="narrow">
        <h1>Transfer successful</h1>
        <p>Reference #{result.transactionId}</p>
        <p>Sent {formatMoney(result.amount)} to {result.recipientName}</p>
        <p>New balance: {formatMoney(result.newBalance)}</p>
        <button onClick={reset}>Make another transfer</button> <Link to="/">Back to dashboard</Link>
      </div>
    )
  }

  if (step === 'details') {
    return (
      <div className="narrow">
        <h1>Transfer money</h1>
        <p>Sending to: <strong>{recipient.fullName}</strong> ({recipient.phoneNumber})</p>
        <form onSubmit={handleSend}>
          <label>
            Amount (₱)
            <input value={amountText} onChange={(e) => setAmountText(e.target.value)} inputMode="decimal" required />
          </label>
          <label>
            Description (optional)
            <input value={description} onChange={(e) => setDescription(e.target.value)} maxLength={255} />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={busy}>{busy ? 'Sending…' : 'Confirm and send'}</button>{' '}
          <button type="button" onClick={reset}>Cancel</button>
        </form>
      </div>
    )
  }

  return (
    <div className="narrow">
      <h1>Transfer money</h1>
      <form onSubmit={handleLookup}>
        <label>
          Recipient phone number
          <input value={phone} onChange={(e) => setPhone(e.target.value)} inputMode="numeric" maxLength={11} required />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={busy}>{busy ? 'Searching…' : 'Find recipient'}</button>
      </form>
    </div>
  )
}
