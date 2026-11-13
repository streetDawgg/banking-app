import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { formatMoney } from '../format.js'
import TransactionList from '../components/TransactionList.jsx'

export default function Dashboard() {
  const [account, setAccount] = useState(null)
  const [recent, setRecent] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    Promise.all([api.account(), api.recent()])
      .then(([acct, txs]) => {
        if (cancelled) return
        setAccount(acct)
        setRecent(txs)
      })
      .catch((err) => !cancelled && setError(err.message))
    return () => {
      cancelled = true
    }
  }, [])

  if (error) return <p className="error">{error}</p>
  if (!account) return <p>Loading…</p>

  return (
    <div>
      <h1>Dashboard</h1>
      <h2>Welcome back, <h1>{account.fullName}</h1></h2>
      <section className="card">
        <p>{account.fullName}</p>
        <p>Phone: {account.phoneNumber}</p>
        <p>Account number: {account.accountNumber}</p>
        <p className="balance">{formatMoney(account.balance)}</p>
        <p>
          <Link to="/transfer">Transfer money</Link>
        </p>
      </section>

      <h2>Recent activity</h2>
      <TransactionList items={recent} />
      <p><Link to="/history">View full history</Link></p>
    </div>
  )
}
