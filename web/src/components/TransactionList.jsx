import { formatDate, formatMoney, isIncoming, TYPE_LABELS } from '../format.js'

// React escapes text automatically, so names/descriptions from the server are safe to render here.
export default function TransactionList({ items }) {
  if (!items || items.length === 0) {
    return <p className="muted">No transactions yet.</p>
  }
  return (
    <table className="tx-table">
      <thead>
        <tr>
          <th>Date</th>
          <th>Type</th>
          <th>Details</th>
          <th className="right">Amount</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {items.map((t) => {
          const failed = t.status === 'FAILED'
          const incoming = isIncoming(t.type)
          const details =
            t.type === 'MONEY_SENT'
              ? `To ${t.receiverAccount}`
              : t.type === 'MONEY_RECEIVED'
                ? `From ${t.senderAccount}`
                : ''
          return (
            <tr key={t.id} className={failed ? 'failed' : incoming ? 'incoming' : 'outgoing'}>
              <td>{formatDate(t.createdAt)}</td>
              <td>{TYPE_LABELS[t.type] ?? t.type}</td>
              <td>
                {details} {t.description && `"${t.description}"`}
              </td>
              <td className="right">
                {failed ? '' : incoming ? '+' : '-'}
                {formatMoney(t.amount)}
              </td>
              <td>{t.status}</td>
            </tr>
          )
        })}
      </tbody>
    </table>
  )
}
