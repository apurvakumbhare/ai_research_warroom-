/**
 * auth-form.js
 * Controls the Sign-In / Register page (signin.html).
 *
 * Responsibilities:
 *  1.  Tab toggle  — switches between "Create Account" and "Log In" panels
 *  2.  Deep-link   — activates the Login panel when URL contains #login
 *  3.  Inline links — "Log in →" and "Create one →" cross-links
 *  4.  Validation  — per-field rules with inline errors; runs on submit + on blur
 *  5.  Password visibility toggle
 *  6.  Mock submit  — simulates auth; replace with real fetch() calls
 */

document.addEventListener('DOMContentLoaded', () => {

    // ── Element refs ────────────────────────────────────────────────────────
    const tabRegister = document.getElementById('tab-register');
    const tabLogin = document.getElementById('tab-login');
    const panelRegister = document.getElementById('panel-register');
    const panelLogin = document.getElementById('panel-login');

    const switchToLogin = document.getElementById('switch-to-login');
    const switchToRegister = document.getElementById('switch-to-register');

    const registerForm = document.getElementById('register-form');
    const loginForm = document.getElementById('login-form');

    const registerBanner = document.getElementById('register-banner');
    const loginBanner = document.getElementById('login-banner');

    // ── 1. Tab toggle ────────────────────────────────────────────────────────
    function activateTab(tab) {
        // tab = 'register' | 'login'
        const isRegister = tab === 'register';

        tabRegister.classList.toggle('active', isRegister);
        tabLogin.classList.toggle('active', !isRegister);

        tabRegister.setAttribute('aria-selected', isRegister);
        tabLogin.setAttribute('aria-selected', !isRegister);

        panelRegister.classList.toggle('active', isRegister);
        panelLogin.classList.toggle('active', !isRegister);

        // Focus first input in the activated panel
        const firstInput = (isRegister ? panelRegister : panelLogin)
            .querySelector('input, select, button[type="submit"]');
        if (firstInput) setTimeout(() => firstInput.focus(), 50);
    }

    tabRegister.addEventListener('click', () => activateTab('register'));
    tabLogin.addEventListener('click', () => activateTab('login'));

    // ── 2. Deep-link: activate login panel if URL hash is #login or #register ──
    if (window.location.hash === '#login') activateTab('login');
    if (window.location.hash === '#register') activateTab('register');

    // ── 3. Cross-links ───────────────────────────────────────────────────────
    if (switchToLogin) switchToLogin.addEventListener('click', () => activateTab('login'));
    if (switchToRegister) switchToRegister.addEventListener('click', () => activateTab('register'));

    // ── 4. Validation helpers ────────────────────────────────────────────────
    const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    const MIN_AGE = 13; // must be at least 13 years old

    function showError(fieldId, errorId, msg) {
        const field = document.getElementById(fieldId);
        const err = document.getElementById(errorId);
        if (!field || !err) return;
        field.classList.add('invalid');
        field.classList.remove('valid');
        err.textContent = msg;
        err.classList.add('visible');
        field.setAttribute('aria-invalid', 'true');
    }

    function clearError(fieldId, errorId) {
        const field = document.getElementById(fieldId);
        const err = document.getElementById(errorId);
        if (!field || !err) return;
        field.classList.remove('invalid');
        field.classList.add('valid');
        err.textContent = '';
        err.classList.remove('visible');
        field.setAttribute('aria-invalid', 'false');
    }

    function showBanner(bannerEl, type, msg) {
        bannerEl.className = `form-banner ${type}`;
        bannerEl.innerHTML = `
            <span class="material-symbols-outlined" style="font-size:18px;">
                ${type === 'success' ? 'check_circle' : 'error'}
            </span>
            ${msg}`;
    }

    function clearBanner(bannerEl) {
        bannerEl.className = 'form-banner';
        bannerEl.innerHTML = '';
    }

    // ── Validate registration fields ─────────────────────────────────────────
    function validateEmail(id, errId) {
        const val = document.getElementById(id).value.trim();
        if (!val) { showError(id, errId, 'Email is required.'); return false; }
        if (!EMAIL_RE.test(val)) { showError(id, errId, 'Enter a valid email address.'); return false; }
        clearError(id, errId); return true;
    }

    function validateName(id, errId) {
        const val = document.getElementById(id).value.trim();
        if (!val) { showError(id, errId, 'Full name is required.'); return false; }
        if (val.length < 2) { showError(id, errId, 'Name must be at least 2 characters.'); return false; }
        clearError(id, errId); return true;
    }

    function validateDob(id, errId) {
        const val = document.getElementById(id).value;
        if (!val) { showError(id, errId, 'Date of birth is required.'); return false; }
        const dob = new Date(val);
        const today = new Date();
        const age = today.getFullYear() - dob.getFullYear()
            - (today < new Date(today.getFullYear(), dob.getMonth(), dob.getDate()) ? 1 : 0);
        if (isNaN(dob.getTime())) { showError(id, errId, 'Enter a valid date.'); return false; }
        if (dob > today) { showError(id, errId, 'Date of birth cannot be in the future.'); return false; }
        if (age < MIN_AGE) { showError(id, errId, `You must be at least ${MIN_AGE} years old.`); return false; }
        clearError(id, errId); return true;
    }

    function validateRole(id, errId) {
        const val = document.getElementById(id).value;
        if (!val) { showError(id, errId, 'Please select a role.'); return false; }
        clearError(id, errId); return true;
    }

    function validatePassword(id, errId) {
        const val = document.getElementById(id).value;
        if (!val) { showError(id, errId, 'Password is required.'); return false; }
        if (val.length < 8) { showError(id, errId, 'Password must be at least 8 characters.'); return false; }
        if (!/[A-Z]/.test(val)) { showError(id, errId, 'Include at least one uppercase letter.'); return false; }
        if (!/[0-9]/.test(val)) { showError(id, errId, 'Include at least one number.'); return false; }
        clearError(id, errId); return true;
    }

    function validateConfirm(confirmId, confirmErrId, passwordId) {
        const pw = document.getElementById(passwordId).value;
        const cfm = document.getElementById(confirmId).value;
        if (!cfm) { showError(confirmId, confirmErrId, 'Please confirm your password.'); return false; }
        if (pw !== cfm) { showError(confirmId, confirmErrId, 'Passwords do not match.'); return false; }
        clearError(confirmId, confirmErrId); return true;
    }

    function validateLoginEmail() { return validateEmail('login-email', 'login-email-error'); }
    function validateLoginPassword() {
        const val = document.getElementById('login-password').value;
        if (!val) { showError('login-password', 'login-password-error', 'Password is required.'); return false; }
        clearError('login-password', 'login-password-error'); return true;
    }

    // ── Blur listeners (real-time feedback) ──────────────────────────────────
    document.getElementById('reg-email')?.addEventListener('blur', () => validateEmail('reg-email', 'reg-email-error'));
    document.getElementById('reg-name')?.addEventListener('blur', () => validateName('reg-name', 'reg-name-error'));
    document.getElementById('reg-dob')?.addEventListener('blur', () => validateDob('reg-dob', 'reg-dob-error'));
    document.getElementById('reg-role')?.addEventListener('change', () => validateRole('reg-role', 'reg-role-error'));
    document.getElementById('reg-password')?.addEventListener('blur', () => validatePassword('reg-password', 'reg-password-error'));
    document.getElementById('reg-confirm')?.addEventListener('blur', () => validateConfirm('reg-confirm', 'reg-confirm-error', 'reg-password'));
    document.getElementById('login-email')?.addEventListener('blur', validateLoginEmail);
    document.getElementById('login-password')?.addEventListener('blur', validateLoginPassword);

    // ── 5. Password show/hide toggle ─────────────────────────────────────────
    document.querySelectorAll('.toggle-pwd').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = document.getElementById(btn.dataset.target);
            if (!input) return;
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            btn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
            btn.querySelector('.material-symbols-outlined').textContent =
                isPassword ? 'visibility_off' : 'visibility';
        });
    });

    // ── 6a. Registration submit ──────────────────────────────────────────────
    registerForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearBanner(registerBanner);

        const valid = [
            validateEmail('reg-email', 'reg-email-error'),
            validateName('reg-name', 'reg-name-error'),
            validateDob('reg-dob', 'reg-dob-error'),
            validateRole('reg-role', 'reg-role-error'),
            validatePassword('reg-password', 'reg-password-error'),
            validateConfirm('reg-confirm', 'reg-confirm-error', 'reg-password'),
        ].every(Boolean);

        if (!valid) {
            showBanner(registerBanner, 'error', 'Please fix the errors above before continuing.');
            return;
        }

        // Collect form data
        const payload = {
            email: document.getElementById('reg-email').value.trim(),
            name: document.getElementById('reg-name').value.trim(),
            dob: document.getElementById('reg-dob').value,
            role: document.getElementById('reg-role').value,
            password: document.getElementById('reg-password').value,
        };

        // ── Real API call ─────────────────────────
        try {
            const res = await fetch('http://localhost:8080/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });
            if (!res.ok) {
                const data = await res.json();
                showBanner(registerBanner, 'error', data.message || 'Registration failed.');
                return;
            }
            const data = await res.json();
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('uid', data.uid);

            showBanner(registerBanner, 'success', 'Account created! Redirecting to dashboard…');
            await mockDelay(1200);
            window.location.href = 'dashboard.html';
        } catch (error) {
            showBanner(registerBanner, 'error', 'Network error occurred.');
        }
    });

    // ── 6b. Login submit ─────────────────────────────────────────────────────
    loginForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearBanner(loginBanner);

        const valid = [validateLoginEmail(), validateLoginPassword()].every(Boolean);
        if (!valid) {
            showBanner(loginBanner, 'error', 'Please fix the errors above.');
            return;
        }

        const payload = {
            email: document.getElementById('login-email').value.trim(),
            password: document.getElementById('login-password').value,
        };

        // ── Real API call ─────────────────────────
        try {
            const res = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });
            if (!res.ok) {
                showBanner(loginBanner, 'error', 'Invalid email or password.');
                return;
            }
            const data = await res.json();
            localStorage.setItem('authToken', data.token);
            // Optionally store user ID too if sent back: localStorage.setItem('uid', data.uid);

            const submitBtn = loginForm.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.textContent = 'Signing in…';
            await mockDelay(700);
            window.location.href = 'dashboard.html';
        } catch (error) {
            showBanner(loginBanner, 'error', 'Network error occurred.');
        }
    });

    // ── Utility ──────────────────────────────────────────────────────────────
    function mockDelay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
});
