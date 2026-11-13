import { useState } from 'react'
import { api } from '../api.js'
import { formatMoney, parseAmount } from '../format.js'

// One form used for both /deposit and /withdraw.
export default function CashForm({ mode }) {
  const [amountText, setAmountText] = useState('')
  const [newBalance, setNewBalance] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const title = mode === 'deposit' ? 'Cash deposit' : 'Cash withdrawal'

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setNewBalance(null)
    const amount = parseAmount(amountText)
    if (amount === null) return setError('Enter a valid amount (up to 2 decimal places)')
    setBusy(true)
    try {
      const res = await api[mode](amount)
      setNewBalance(res.balance)
      setAmountText('')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="narrow">
      <h1>{title}</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Amount (₱)
          <input value={amountText} onChange={(e) => setAmountText(e.target.value)} inputMode="decimal" required />
        </label>
        {error && <p className="error">{error}</p>}
        {newBalance !== null && <p className="success">Done. New balance: {formatMoney(newBalance)}</p>}
        <button type="submit" disabled={busy}>{busy ? 'Processing…' : title}</button>
      </form>
    </div>
  )
}
