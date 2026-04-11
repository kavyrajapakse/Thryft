import { auth } from "./firebase.js";
import { signInWithEmailAndPassword, onAuthStateChanged, signOut } from
"https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";

const ALLOWED_ADMIN_EMAIL = "camidex.labs@gmail.com";

function normalizeEmail(value) {
    return String(value || "").trim().toLowerCase();
}

// ---------------- CHECK IF ALREADY LOGGED IN ----------------
onAuthStateChanged(auth, async (user) => {
    if (user) {
        if (normalizeEmail(user.email) === ALLOWED_ADMIN_EMAIL) {
            window.location.href = "dashboard.html";
            return;
        }

        await signOut(auth);
        window.showToast('Access Denied', 'Only the configured admin email can sign in.', 'error');
    }
});

document.getElementById('loginForm').addEventListener('submit', function(e){

    e.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (normalizeEmail(email) !== ALLOWED_ADMIN_EMAIL) {
        document.getElementById('errorMessage').classList.remove('hidden');
        document.getElementById('errorText').textContent = 'Only the admin email is allowed.';
        window.showToast('Login Failed', 'Only the configured admin email can sign in.', 'error');
        return;
    }

    signInWithEmailAndPassword(auth, email, password)

    .then((userCredential)=>{
        if (normalizeEmail(userCredential.user.email) !== ALLOWED_ADMIN_EMAIL) {
            return signOut(auth).then(() => {
                throw new Error('Unauthorized account');
            });
        }

        window.showToast('Success!', 'Login successful! Redirecting...', 'success');

        setTimeout(() => {
            window.location.href="dashboard.html";
        }, 1500);

    })

    .catch((error)=>{

        document.getElementById('errorMessage').classList.remove('hidden');
        document.getElementById('errorText').textContent = error.message;

        window.showToast('Login Failed', 'Invalid email or password', 'error');

    });

});