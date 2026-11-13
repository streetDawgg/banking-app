import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'

export default function Layout() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <div className="app">
      <header className="nav">
        <strong>Java Bank</strong>
        <nav>
          <NavLink to="/" end>Dashboard</NavLink>
          <NavLink to="/transfer">Transfer</NavLink>
          <NavLink to="/deposit">Deposit</NavLink>
          <NavLink to="/withdraw">Withdraw</NavLink>
          <NavLink to="/history">History</NavLink>
        </nav>
        <button onClick={handleLogout}>Log out</button>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </div>
  )
}
