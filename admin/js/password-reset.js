const API_BASE_URL = "http://localhost:4000";

function showToast(title, message, type = "info") {
  let toast = document.getElementById("toast");
  if (!toast) {
    alert(message);
    return;
  }

  const toastContent = document.getElementById("toastContent");
  const toastIcon = document.getElementById("toastIcon");
  const toastTitle = document.getElementById("toastTitle");
  const toastMessage = document.getElementById("toastMessage");

  if (type === "success") {
    toastIcon.className = "fas fa-check-circle text-2xl text-success";
    toastContent.style.borderLeftColor = "#16A34A";
  } else if (type === "error") {
    toastIcon.className = "fas fa-exclamation-circle text-2xl text-error";
    toastContent.style.borderLeftColor = "#DC2626";
  } else {
    toastIcon.className = "fas fa-info-circle text-2xl text-info";
    toastContent.style.borderLeftColor = "#2563EB";
  }

  toastTitle.textContent = title;
  toastMessage.textContent = message;
  toast.classList.remove("hidden");

  setTimeout(() => {
    toast.classList.add("hidden");
  }, 3000);
}

async function requestPasswordReset(email) {
  const response = await fetch(`${API_BASE_URL}/api/password-reset/request`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email })
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || "Failed to send reset link");
  }

  return data;
}

async function verifyToken(token) {
  const response = await fetch(`${API_BASE_URL}/api/password-reset/verify`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ token })
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || "Invalid token");
  }

  return data;
}

async function confirmPasswordReset(token, newPassword) {
  const response = await fetch(`${API_BASE_URL}/api/password-reset/confirm`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ token, newPassword })
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || "Failed to reset password");
  }

  return data;
}

function initForgotPasswordPage() {
  const form = document.getElementById("forgotPasswordForm");
  if (!form) return;

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const email = document.getElementById("resetEmail").value.trim();

    try {
      await requestPasswordReset(email);
      showToast("Request Submitted", "If the account exists, a reset link was sent to the email address.", "success");
      form.reset();
    } catch (error) {
      showToast("Request Failed", error.message, "error");
    }
  });
}

function initResetPasswordPage() {
  const form = document.getElementById("resetPasswordForm");
  if (!form) return;

  const params = new URLSearchParams(window.location.search);
  const token = params.get("token");

  if (!token) {
    showToast("Invalid Link", "Missing reset token", "error");
    form.querySelector("button[type='submit']").disabled = true;
    return;
  }

  verifyToken(token).catch((error) => {
    showToast("Invalid Token", error.message, "error");
    form.querySelector("button[type='submit']").disabled = true;
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (newPassword !== confirmPassword) {
      showToast("Validation Error", "Passwords do not match", "error");
      return;
    }

    if (newPassword.length < 8) {
      showToast("Validation Error", "Password must be at least 8 characters", "error");
      return;
    }

    try {
      await confirmPasswordReset(token, newPassword);
      showToast("Success", "Password updated. Redirecting to login...", "success");
      setTimeout(() => {
        window.location.href = "index.html";
      }, 1500);
    } catch (error) {
      showToast("Reset Failed", error.message, "error");
    }
  });
}

initForgotPasswordPage();
initResetPasswordPage();
