// All communication with the Spring Boot server lives here.
// Paths are relative ("/api/..."): in development Vite proxies them to http://localhost:8080.

const TOKEN_KEY = 'bank_token'

// localStorage is simple but readable by any script on the page.
// An HttpOnly cookie is the safer upgrade for later (needs backend changes).
export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token) =>
  token ? localStorage.setItem(TOKEN_KEY, token) : localStorage.removeItem(TOKEN_KEY)

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

// AuthContext registers a function here that logs the user out when the token is rejected/expired.
let onUnauthorized = () => {}
export const setUnauthorizedHandler = (fn) => {
  onUnauthorized = fn
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = auth ? getToken() : null
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Is it running?')
  }

  let data = null
  const text = await res.text()
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      /* non-JSON body */
    }
  }

  if (!res.ok) {
    const message = data?.message ?? (res.status === 401 ? 'Not authorized' : `Request failed (${res.status})`)
    if (res.status === 401 && token) onUnauthorized()
    throw new ApiError(res.status, message)
  }
  return data
}

export const api = {
  register: (body) => request('/api/auth/register', { method: 'POST', body, auth: false }),
  login: (body) => request('/api/auth/login', { method: 'POST', body, auth: false }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),

  account: () => request('/api/accounts/me'),
  balance: () => request('/api/accounts/balance'),
  deposit: (amount) => request('/api/accounts/deposit', { method: 'POST', body: { amount } }),
  withdraw: (amount) => request('/api/accounts/withdraw', { method: 'POST', body: { amount } }),

  recent: () => request('/api/transactions/recent'),
  history: (page = 0, size = 10) => request(`/api/transactions/history?page=${page}&size=${size}`),

  recipient: (phone) => request(`/api/transfers/recipient/${encodeURIComponent(phone)}`),
  transfer: (body) => request('/api/transfers', { method: 'POST', body }),
}
