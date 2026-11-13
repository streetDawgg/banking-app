# bank-web (React + Vite starter)

Put this folder at `banking-app/web`. Do NOT add it to the `<modules>` list in the root pom.xml.

```bash
cd web
npm install
npm run dev        # http://localhost:5173
```

Start the Spring Boot server on port 8080 first. Vite proxies `/api/*` to it (see vite.config.js),
so no CORS setup is needed in development.

If you change the server port, edit the proxy target in `vite.config.js`.

## Layout
- `src/api.js`            all HTTP calls + token storage + 401 handling
- `src/AuthContext.jsx`   login/logout state
- `src/App.jsx`           routes (login/register public, the rest protected)
- `src/pages/*`           Login, Register, Dashboard, Transfer, CashForm (deposit/withdraw), History
- `src/components/*`      Layout (nav bar), TransactionList
- `src/format.js`         ₱ formatting, date formatting, amount parsing
- `src/index.css`         minimal placeholder styling, replace freely

## Notes
- Token lives in localStorage (fine for learning; an HttpOnly cookie is the safer upgrade later).
- Tokens last 15 minutes; a 401 logs the user out and shows "session expired" on the login page.
- For production you'd need HTTPS and either serving `dist/` from Spring Boot or enabling CORS.
