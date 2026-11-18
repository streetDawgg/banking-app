import { Outlet } from 'react-router-dom'
import Sidebar from './sidebar.jsx'

export default function Layout() {
  return (
    <div className="app">
      <Sidebar />

      <div className="layout-content">
        <main className="page">
          <Outlet />
        </main>
      </div>
    </div>
  )
}