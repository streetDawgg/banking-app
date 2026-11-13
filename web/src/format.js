const peso = new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' })

export const formatMoney = (n) => peso.format(n)

export const formatDate = (iso) =>
  new Date(iso).toLocaleString('en-PH', { dateStyle: 'medium', timeStyle: 'short' })

// Up to 12 digits and at most 2 decimals, matching the server's validation.
const AMOUNT_PATTERN = /^\d{1,12}(\.\d{1,2})?$/

/** Returns a positive number, or null if the text isn't a valid amount. */
export function parseAmount(input) {
  const cleaned = input.replace(/,/g, '').trim()
  if (!AMOUNT_PATTERN.test(cleaned)) return null
  const value = Number(cleaned)
  return value > 0 ? value : null
}

export const TYPE_LABELS = {
  MONEY_SENT: 'Money sent',
  MONEY_RECEIVED: 'Money received',
  CASH_DEPOSIT: 'Cash deposit',
  CASH_WITHDRAWAL: 'Cash withdrawal',
}

export const isIncoming = (type) => type === 'MONEY_RECEIVED' || type === 'CASH_DEPOSIT'
