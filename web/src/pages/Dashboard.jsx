import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { formatMoney } from '../format.js'
import TransactionList from '../components/TransactionList.jsx'
import '../styles/dashboard.css'

export default function Dashboard() {
  const [account, setAccount] = useState(null)
  const [recent, setRecent] = useState([])
  const [error, setError] = useState('')
  const [showBalance, setShowBalance] = useState(true)

  useEffect(() => {
    let cancelled = false

    Promise.all([api.account(), api.recent()])
      .then(([acct, txs]) => {
        if (cancelled) return

        setAccount(acct)
        setRecent(txs)
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message || 'Unable to load account information.')
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  // Mask account number, showing only the last 4 digits
  const maskedAccount = account?.accountNumber
    ? `---- ---- ${String(account.accountNumber).slice(-4)}`
    : 'Not available'

  if (error) {
    return (
      <div className="dashboard-page">
        <div className="dashboard-message error">
          <h2>Unable to load dashboard</h2>
          <p>{error}</p>
          <button onClick={() => window.location.reload()}>
            Try Again
          </button>
        </div>
      </div>
    )
  }

  if (!account) {
    return (
      <div className="dashboard-page">
        <div className="dashboard-loading">
          <div className="loading-spinner"></div>
          <p>Loading your banking dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="dashboard-page">

      {/* Welcome Section */}
      <section className="dashboard-welcome">
        <div>
          <p className="dashboard-eyebrow">
            YOUR FINANCIAL OVERVIEW
          </p>

          <h1>
            Welcome back, {account.fullName.split(' ')[0]}!
          </h1>

          <p className="welcome-description">
            Here's an overview of your account and recent activities.
          </p>
        </div>

        <div className="account-status">
          <span className="status-dot"></span>
          Account Active
        </div>
      </section>


      {/* Account Overview */}
      <section className="dashboard-overview">

        {/* Balance Card */}
        <div className="dashboard-balance-card">

          <div className="balance-card-top">
            <div>
              <p className="balance-label">
                Available Balance
              </p>

              <p className="balance-caption">
                Your current account balance
              </p>
            </div>

            <button
              type="button"
              className="balance-visibility"
              onClick={() => setShowBalance((prev) => !prev)}
              aria-label={showBalance ? 'Hide balance' : 'Show balance'}
            >
              {showBalance ? 'Hide' : 'Show'}
            </button>
          </div>

          <h2 className="dashboard-balance">
            {showBalance
              ? formatMoney(account.balance)
              : '₱ ••••••••'}
          </h2>

          <div className="balance-card-bottom">
            <div>
              <p>ACCOUNT NUMBER</p>
              <strong>{maskedAccount}</strong>
            </div>

            <div className="balance-card-brand">
              MYBANK
            </div>
          </div>

        </div>


        {/* Account Information */}
        <div className="dashboard-account-card">

          <div className="account-card-heading">
            <div className="account-avatar">
              {account.fullName
                .split(' ')
                .slice(0, 2)
                .map((name) => name[0])
                .join('')
                .toUpperCase()}
            </div>

            <div>
              <p>Account Holder</p>
              <h3>{account.fullName}</h3>
            </div>
          </div>

          <div className="account-info-list">

            <div className="account-info-item">
              <span>Phone Number</span>
              <strong>{account.phoneNumber}</strong>
            </div>

            <div className="account-info-item">
              <span>Account Number</span>
              <strong>{maskedAccount}</strong>
            </div>

          </div>

          <div className="account-card-footer">
            <span className="status-dot"></span>
            <span>Personal Account</span>
          </div>

        </div>

      </section>


      {/* Quick Actions */}
      <section className="dashboard-actions">

        <div className="dashboard-section-heading">
          <div>
            <h2>Quick Actions</h2>
            <p>Manage your money easily.</p>
          </div>
        </div>

        <div className="quick-action-grid">

          <Link to="/transfer" className="quick-action-card">
            <div className="quick-action-icon send-action">
              <span>↗</span>
            </div>

            <div>
              <h3>Send Money</h3>
              <p>Transfer money to another account</p>
            </div>

            <span className="quick-action-arrow">→</span>
          </Link>

          <Link to="/history" className="quick-action-card">
            <div className="quick-action-icon history-action">
              <span>↔</span>
            </div>

            <div>
              <h3>Transaction History</h3>
              <p>Review your previous transactions</p>
            </div>

            <span className="quick-action-arrow">→</span>
          </Link>

        </div>

      </section>


      {/* Recent Transactions */}
      <section className="dashboard-transactions">

        <div className="dashboard-section-heading transaction-heading">
          <div>
            <h2>Recent Activity</h2>
            <p>Keep track of your latest transactions.</p>
          </div>

          <Link to="/history" className="dashboard-view-all">
            View All →
          </Link>
        </div>

        <div className="dashboard-transaction-card">
          <TransactionList items={recent} />
        </div>

      </section>


      {/* Footer */}
      <footer className="dashboard-footer">
        <p>© 2026 MYBANK. All rights reserved.</p>
        <p>Secure Banking Experience</p>
      </footer>

    </div>
  )
}
