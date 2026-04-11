const express = require("express");
const cors = require("cors");
const dotenv = require("dotenv");
const crypto = require("crypto");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

dotenv.config();

const app = express();
const port = Number(process.env.PORT || 4000);
const appBaseUrl = process.env.APP_BASE_URL || "http://127.0.0.1:5500";
const resetTokenTtlMinutes = Number(process.env.RESET_TOKEN_TTL_MINUTES || 20);
const allowedOrigins = (process.env.ALLOWED_ORIGINS || "").split(",").map((v) => v.trim()).filter(Boolean);
const allowInsecureTerminalResetLinkLog =
  String(process.env.ALLOW_INSECURE_TERMINAL_RESET_LINK_LOG || "false").toLowerCase() === "true";

app.use(express.json());
app.use(
  cors({
    origin(origin, callback) {
      if (!origin) {
        return callback(null, true);
      }

      if (allowedOrigins.length === 0 || allowedOrigins.includes(origin) || allowedOrigins.includes("*")) {
        return callback(null, true);
      }

      return callback(new Error("CORS blocked"));
    }
  })
);

function initializeFirebaseAdmin() {
  const projectId = String(process.env.FIREBASE_PROJECT_ID || "").trim();
  const clientEmail = String(process.env.FIREBASE_CLIENT_EMAIL || "").trim();
  const privateKey = process.env.FIREBASE_PRIVATE_KEY
    ? process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, "\n").trim()
    : undefined;

  if (!projectId || !clientEmail || !privateKey) {
    throw new Error("Missing Firebase Admin credentials in .env. Create .env from .env.example and fill FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL, and FIREBASE_PRIVATE_KEY.");
  }

  admin.initializeApp({
    credential: admin.credential.cert({
      projectId,
      clientEmail,
      privateKey
    })
  });
}

initializeFirebaseAdmin();

// In-memory token store for dev usage.
// For production, replace with Redis or database storage.
const resetTokenStore = new Map();

function hashToken(rawToken) {
  return crypto.createHash("sha256").update(rawToken).digest("hex");
}

function createRawToken() {
  return crypto.randomBytes(32).toString("hex");
}

function getTransporter() {
  const smtpHost = process.env.SMTP_HOST;
  const smtpUser = process.env.SMTP_USER;
  const smtpPass = process.env.SMTP_PASS;

  if (!smtpHost || !smtpUser || !smtpPass) {
    return null;
  }

  return nodemailer.createTransport({
    host: smtpHost,
    port: Number(process.env.SMTP_PORT || 587),
    secure: String(process.env.SMTP_SECURE || "false") === "true",
    auth: {
      user: smtpUser,
      pass: smtpPass
    }
  });
}

async function sendResetLink(email, resetLink) {
  const transporter = getTransporter();

  if (!transporter) {
    if (allowInsecureTerminalResetLinkLog) {
      console.warn("[INSECURE-DEV-ONLY] Password reset link for", email, resetLink);
      return;
    }

    throw new Error(
      "SMTP is not configured. Refusing to log reset links to terminal. Configure SMTP or set ALLOW_INSECURE_TERMINAL_RESET_LINK_LOG=true for local testing only."
    );
  }

  const from = process.env.SMTP_FROM || "no-reply@example.com";

  await transporter.sendMail({
    from,
    to: email,
    subject: "Reset your admin password",
    text: `Use this link to reset your password: ${resetLink}`,
    html: `<p>Use this link to reset your admin password:</p><p><a href=\"${resetLink}\">${resetLink}</a></p>`
  });
}

function cleanupExpiredTokens() {
  const now = Date.now();
  for (const [tokenHash, record] of resetTokenStore.entries()) {
    if (record.expiresAt <= now) {
      resetTokenStore.delete(tokenHash);
    }
  }
}

setInterval(cleanupExpiredTokens, 60 * 1000).unref();

app.get("/health", (_req, res) => {
  res.json({ ok: true });
});

app.post("/api/password-reset/request", async (req, res) => {
  const email = String(req.body?.email || "").trim().toLowerCase();

  if (!email) {
    return res.status(400).json({ message: "Email is required" });
  }

  try {
    const userRecord = await admin.auth().getUserByEmail(email);
    const rawToken = createRawToken();
    const tokenHash = hashToken(rawToken);
    const expiresAt = Date.now() + resetTokenTtlMinutes * 60 * 1000;

    resetTokenStore.set(tokenHash, {
      uid: userRecord.uid,
      email,
      expiresAt
    });

    const resetLink = `${appBaseUrl}/reset-password.html?token=${rawToken}`;
    await sendResetLink(email, resetLink);
  } catch (error) {
    console.error("Password reset request processing error:", error.message);
    // Avoid exposing whether the email exists.
  }

  return res.json({
    message: "If an account exists for this email, a reset link has been sent."
  });
});

app.post("/api/password-reset/verify", (req, res) => {
  const token = String(req.body?.token || "").trim();
  if (!token) {
    return res.status(400).json({ message: "Token is required" });
  }

  const tokenHash = hashToken(token);
  const record = resetTokenStore.get(tokenHash);

  if (!record || record.expiresAt < Date.now()) {
    return res.status(400).json({ valid: false, message: "Invalid or expired token" });
  }

  return res.json({ valid: true });
});

app.post("/api/password-reset/confirm", async (req, res) => {
  const token = String(req.body?.token || "").trim();
  const newPassword = String(req.body?.newPassword || "");

  if (!token || !newPassword) {
    return res.status(400).json({ message: "Token and newPassword are required" });
  }

  if (newPassword.length < 8) {
    return res.status(400).json({ message: "Password must be at least 8 characters" });
  }

  const tokenHash = hashToken(token);
  const record = resetTokenStore.get(tokenHash);

  if (!record || record.expiresAt < Date.now()) {
    return res.status(400).json({ message: "Invalid or expired token" });
  }

  try {
    await admin.auth().updateUser(record.uid, { password: newPassword });
    resetTokenStore.delete(tokenHash);
    return res.json({ message: "Password has been reset successfully" });
  } catch (_error) {
    return res.status(500).json({ message: "Failed to reset password" });
  }
});

app.listen(port, () => {
  console.log(`Password reset API running on http://localhost:${port}`);
});
