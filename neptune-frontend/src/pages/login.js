import { AuthAPI, setAuth } from '../api.js';
import { showToast } from '../utils.js';
import { navigate } from '../router.js';

export async function renderLogin() {
    const app = document.getElementById('app');
    app.innerHTML = `
        <div class="auth-wrapper">
            <div class="auth-card">
                <div class="auth-logo">
                    <h1>Neptune Bank</h1>
                    <p>Sign in to your account</p>
                </div>
                <form id="login-form">
                    <div class="form-group">
                        <label for="login-username">Username</label>
                        <input type="text" id="login-username" class="form-input" placeholder="Enter your username" required autocomplete="username" />
                    </div>
                    <div class="form-group">
                        <label for="login-password">Password</label>
                        <input type="password" id="login-password" class="form-input" placeholder="Enter your password" required autocomplete="current-password" />
                    </div>
                    <div class="form-group">
                        <label for="login-mode">Authentication Mode</label>
                        <select id="login-mode" class="form-select">
                            <option value="jwt">JWT Token</option>
                            <option value="session">Session</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary btn-full" id="login-btn">
                        Sign In
                    </button>
                </form>
                <div class="auth-footer">
                    Don't have an account? <a href="#/register">Create one</a>
                </div>
            </div>
        </div>
    `;

    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('login-btn');
        const username = document.getElementById('login-username').value.trim();
        const password = document.getElementById('login-password').value;
        const mode = document.getElementById('login-mode').value;

        if (!username || !password) {
            showToast('Please fill in all fields', 'error');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Signing in...';

        try {
            const res = await AuthAPI.login(username, password, mode);
            setAuth(res.token, { username: res.username, role: res.role });
            showToast('Login successful', 'success');
            navigate('/dashboard');
        } catch (err) {
            showToast(err.message || 'Login failed', 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Sign In';
        }
    });
}
