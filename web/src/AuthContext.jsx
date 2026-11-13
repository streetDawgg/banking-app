import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api, getToken, setToken, setUnauthorizedHandler } from './api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(getToken())
  const [sessionExpired, setSessionExpired] = useState(false)

  const clear = useCallback(() => {
    setToken(null)
    setTokenState(null)
  }, [])

  // If any API call comes back 401 while logged in (e.g. the 15-minute token expired), log out.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      clear()
      setSessionExpired(true)
    })
  }, [clear])

  const login = async (phoneNumber, pin) => {
    const res = await api.login({ phoneNumber, pin })
    setToken(res.token)
    setTokenState(res.token)
    setSessionExpired(false)
  }

  const logout = async () => {
    try {
      await api.logout() // revokes the token on the server; ignore failures
    } catch {
      /* ignore */
    }
    clear()
    setSessionExpired(false)
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated: !!token, sessionExpired, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext)
