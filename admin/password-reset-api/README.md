# Password Reset API (Node.js + Express)

This API provides a backend-based password reset flow for your Firebase admin users.

## Endpoints

- `POST /api/password-reset/request`
  - body: `{ "email": "admin@example.com" }`
  - Always returns a generic success response.
- `POST /api/password-reset/verify`
  - body: `{ "token": "..." }`
  - Checks if token is valid and not expired.
- `POST /api/password-reset/confirm`
  - body: `{ "token": "...", "newPassword": "new-strong-pass" }`
  - Updates user password in Firebase Auth.

## Setup

1. Open terminal in `password-reset-api`.
2. Run `npm install`.
3. Copy `.env.example` to `.env`.
4. Fill Firebase Admin credentials from your service account.
5. Fill SMTP details to email links (recommended).
6. Run `npm run dev`.

## Frontend URLs

- Request page: `forgot-password.html`
- Reset page: `reset-password.html?token=<token>`

If SMTP is not configured, the API will refuse to print reset links to terminal by default.
For local-only testing, set `ALLOW_INSECURE_TERMINAL_RESET_LINK_LOG=true` in `.env`.
