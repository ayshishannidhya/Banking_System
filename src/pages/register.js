import { AuthAPI } from '../api.js';
import { showToast } from '../utils.js';
import { navigate } from '../router.js';

export async function renderRegister() {
    const app = document.getElementById('app');
    app.innerHTML = `
        <div class="auth-wrapper">
            <div class="auth-card">
                <div class="auth-logo">
                    <h1>Neptune Bank</h1>
                    <p>Create a new account</p>
                </div>
                <form id="register-form">
                    <div class="form-group">
                        <label for="reg-username">Username</label>
                        <input type="text" id="reg-username" class="form-input" placeholder="Choose a username" required autocomplete="username" />
                    </div>
                    <div class="form-group">
                        <label for="reg-password">Password</label>
                        <input type="password" id="reg-password" class="form-input" placeholder="Create a strong password" required autocomplete="new-password" />
                    </div>
                    <div class="form-group">
                        <label for="reg-confirm">Confirm Password</label>
                        <input type="password" id="reg-confirm" class="form-input" placeholder="Re-enter your password" required autocomplete="new-password" />
                    </div>
                    <div class="form-group">
                        <label for="reg-role">Account Type</label>
                        <select id="reg-role" class="form-select">
                            <option value="USER">Customer</option>
                            <option value="EMPLOYEE">Employee</option>
                            <option value="ADMIN">Administrator</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary btn-full" id="reg-btn">
                        Create Account
                    </button>
                </form>
                <div class="auth-footer">
                    Already have an account? <a href="#/login">Sign in</a>
                </div>
            </div>
        </div>
    `;

    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('reg-btn');
        const username = document.getElementById('reg-username').value.trim();
        const password = document.getElementById('reg-password').value;
        const confirm = document.getElementById('reg-confirm').value;
        const role = document.getElementById('reg-role').value;

        if (!username || !password) {
            showToast('Please fill in all fields', 'error');
            return;
        }

        if (password !== confirm) {
            showToast('Passwords do not match', 'error');
            return;
        }

        if (password.length < 6) {
            showToast('Password must be at least 6 characters', 'error');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Creating...';

        try {
            await AuthAPI.register(username, password, role);
            showToast('Account created successfully! Please login.', 'success');
            navigate('/login');
        } catch (err) {
            showToast(err.message || 'Registration failed', 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Create Account';
        }
    });
}
