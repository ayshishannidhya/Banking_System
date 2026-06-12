import './style.css';
import { registerRoute, startRouter, navigate } from './router.js';
import { isLoggedIn } from './api.js';
import { renderLogin } from './pages/login.js';
import { renderRegister } from './pages/register.js';
import { renderDashboard } from './pages/dashboard.js';
import { renderAccounts } from './pages/accounts.js';
import { renderTransactions } from './pages/transactions.js';
import { renderTransfer } from './pages/transfer.js';
import { renderOtp } from './pages/otp.js';
import { renderProfile } from './pages/profile.js';

function authGuard(handler) {
    return async (params) => {
        if (!isLoggedIn()) {
            navigate('/login');
            return;
        }
        return handler(params);
    };
}

function guestOnly(handler) {
    return async (params) => {
        if (isLoggedIn()) {
            navigate('/dashboard');
            return;
        }
        return handler(params);
    };
}

registerRoute('/login', guestOnly(renderLogin));
registerRoute('/register', guestOnly(renderRegister));
registerRoute('/dashboard', authGuard(renderDashboard));
registerRoute('/accounts', authGuard(renderAccounts));
registerRoute('/transactions', authGuard(renderTransactions));
registerRoute('/transfer', authGuard(renderTransfer));
registerRoute('/otp', authGuard(renderOtp));
registerRoute('/profile', authGuard(renderProfile));

startRouter();
