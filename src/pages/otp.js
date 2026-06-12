import { OtpAPI } from '../api.js';
import { renderLayout } from '../layout.js';
import { showToast } from '../utils.js';

export async function renderOtp() {
    renderLayout('OTP Service', 'Send and verify one-time passwords', '');

    const content = document.getElementById('page-content');
    content.innerHTML = `
        <div class="grid-2">
            <div class="card">
                <div class="card-header">
                    <h2>Send OTP</h2>
                    <span class="material-icons-round" style="color:var(--accent)">send</span>
                </div>
                <div class="card-body">
                    <form id="send-otp-form">
                        <div class="form-group">
                            <label>Phone Number</label>
                            <input type="tel" class="form-input" id="otp-phone" placeholder="+919876543210" />
                        </div>
                        <div class="form-group">
                            <label>Email Address</label>
                            <input type="email" class="form-input" id="otp-email" placeholder="user@example.com" />
                        </div>
                        <p style="font-size:0.78rem;color:var(--text-muted);margin-bottom:1rem">At least one of phone or email is required.</p>
                        <button type="submit" class="btn btn-primary btn-full" id="send-otp-btn">
                            <span class="material-icons-round" style="font-size:1.1rem">send</span>
                            Send OTP
                        </button>
                    </form>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h2>Verify OTP</h2>
                    <span class="material-icons-round" style="color:var(--success)">verified</span>
                </div>
                <div class="card-body">
                    <form id="verify-otp-form">
                        <div class="form-group">
                            <label>Phone or Email</label>
                            <input type="text" class="form-input" id="verify-identifier" placeholder="Phone number or email" required />
                        </div>
                        <div class="form-group">
                            <label>OTP Code</label>
                            <input type="text" class="form-input" id="verify-code" placeholder="Enter 6-digit OTP" maxlength="6" required style="font-size:1.5rem;text-align:center;letter-spacing:8px;font-weight:700" />
                        </div>
                        <button type="submit" class="btn btn-success btn-full" id="verify-otp-btn">
                            <span class="material-icons-round" style="font-size:1.1rem">check_circle</span>
                            Verify OTP
                        </button>
                    </form>
                    <div id="verify-result" style="margin-top:1rem"></div>
                </div>
            </div>
        </div>
    `;

    document.getElementById('send-otp-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('send-otp-btn');
        const phone = document.getElementById('otp-phone').value.trim();
        const email = document.getElementById('otp-email').value.trim();

        if (!phone && !email) {
            showToast('Please provide a phone number or email', 'error');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Sending...';

        try {
            await OtpAPI.send(phone || null, email || null);
            showToast('OTP sent successfully!', 'success');
            if (phone) document.getElementById('verify-identifier').value = phone;
            else if (email) document.getElementById('verify-identifier').value = email;
        } catch (err) {
            showToast(err.message || 'Failed to send OTP', 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<span class="material-icons-round" style="font-size:1.1rem">send</span> Send OTP';
        }
    });

    document.getElementById('verify-otp-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('verify-otp-btn');
        const identifier = document.getElementById('verify-identifier').value.trim();
        const otp = document.getElementById('verify-code').value.trim();

        if (!identifier || !otp) {
            showToast('Please fill in all fields', 'error');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Verifying...';

        try {
            const res = await OtpAPI.verify(identifier, otp);
            document.getElementById('verify-result').innerHTML = `
                <div style="text-align:center;padding:1rem;background:var(--success-bg);border-radius:var(--radius-sm);border:1px solid rgba(34,197,94,0.3)">
                    <span class="material-icons-round" style="font-size:2rem;color:var(--success)">verified</span>
                    <p style="color:var(--success);font-weight:600;margin-top:0.5rem">OTP Verified Successfully</p>
                </div>
            `;
            showToast('OTP verified!', 'success');
        } catch (err) {
            document.getElementById('verify-result').innerHTML = `
                <div style="text-align:center;padding:1rem;background:var(--danger-bg);border-radius:var(--radius-sm);border:1px solid rgba(239,68,68,0.3)">
                    <span class="material-icons-round" style="font-size:2rem;color:var(--danger)">cancel</span>
                    <p style="color:var(--danger);font-weight:600;margin-top:0.5rem">${err.message || 'Verification Failed'}</p>
                </div>
            `;
            showToast(err.message || 'OTP verification failed', 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<span class="material-icons-round" style="font-size:1.1rem">check_circle</span> Verify OTP';
        }
    });
}
