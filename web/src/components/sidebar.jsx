
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'
import '../styles/sidebar.css'

export default function Sidebar() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <aside className="sidebar">
  
      <div className="sidebar-brand">
        <div className="sidebar-brand-icon"><img src="/BB.png" alt="Bank of Banks Logo" /></div>
        <h2>Bank of Banks</h2>
      </div>

      <div className="sidebar-menu">
        <p className="sidebar-label">MENU</p>

        <nav className="sidebar-nav">
          <NavLink to="/" end className="sidebar-link">
            <span className="sidebar-icon">⌂</span>
            <span>Dashboard</span>
          </NavLink>

          <NavLink to="/transfer" className="sidebar-link">
            <span className="sidebar-icon">↗</span>
            <span>Transfer</span>
          </NavLink>

          <NavLink to="/deposit" className="sidebar-link">
            <span className="sidebar-icon">＋</span>
            <span>Deposit</span>
          </NavLink>

          <NavLink to="/withdraw" className="sidebar-link">
            <span className="sidebar-icon">－</span>
            <span>Withdraw</span>
          </NavLink>

          <NavLink to="/history" className="sidebar-link">
            <span className="sidebar-icon">↔</span>
            <span>Transaction History</span>
          </NavLink>
        </nav>
      </div>

  
      <div className="sidebar-footer">
        <button
          type="button"
          className="sidebar-logout"
          onClick={handleLogout}
        >
          <span className="sidebar-icon">⇥</span>
          <span>Log out</span>
        </button>
      </div>
    </aside>
  )
}
