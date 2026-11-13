import { useEffect, useState } from 'react'
import { api } from '../api.js'
import TransactionList from '../components/TransactionList.jsx'

const PAGE_SIZE = 10

export default function History() {
  const [page, setPage] = useState(0)
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    api
      .history(page, PAGE_SIZE)
      .then((data) => !cancelled && setItems(data))
      .catch((err) => !cancelled && setError(err.message))
    return () => {
      cancelled = true
    }
  }, [page])

  if (error) return <p className="error">{error}</p>

  return (
    <div>
      <h1>Transaction history</h1>
      {items === null ? <p>Loading…</p> : <TransactionList items={items} />}
      <p>
        <button onClick={() => setPage(page - 1)} disabled={page === 0}>Previous</button>{' '}
        <span>Page {page + 1}</span>{' '}
        <button onClick={() => setPage(page + 1)} disabled={!items || items.length < PAGE_SIZE}>Next</button>
      </p>
    </div>
  )
}
