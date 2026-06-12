import { getUser, clearAuth } from './api.js';
import { navigate } from './router.js';
import { getInitials } from './utils.js';

const NAV_ITEMS = [
    { section: 'Main', items: [
        { path: '/dashboard', icon: 'dashboard', label: 'Dashboard' },
        { path: '/accounts', icon: 'account_balance', label: 'Accounts' },
    ]},
    { section: 'Transactions', items: [
        { path: '/transfer', icon: 'swap_horiz', label: 'Fund Transfer' },
        { path: '/transactions', icon: 'receipt_long', label: 'History' },
    ]},
    { section: 'Services', items: [
        { path: '/otp', icon: 'pin', label: 'OTP Service' },
        { path: '/profile', icon: 'person', label: 'Profile' },
    ]}
];

export function renderLayout(pageTitle, subtitle, content) {
    const user = getUser();
    const currentPath = window.location.hash.slice(1) || '/dashboard';

    const navHtml = NAV_ITEMS.map(section => `
        <div class="sidebar-section">
            <div class="sidebar-section-title">${section.section}</div>
            ${section.items.map(item => `
                <div class="nav-item ${currentPath === item.path ? 'active' : ''}" data-nav="${item.path}">
                    <span class="material-icons-round">${item.icon}</span>
                    ${item.label}
                </div>
            `).join('')}
        </div>
    `).join('');

    const app = document.getElementById('app');
    app.innerHTML = `
        <div class="app-layout">
            <aside class="sidebar" id="sidebar">
                <div class="sidebar-logo">
                    <h2>Neptune Bank</h2>
                    <span>Online Banking</span>
                </div>
                <nav class="sidebar-nav">${navHtml}</nav>
                <div class="sidebar-user">
                    <div class="sidebar-user-avatar">${getInitials(user?.username || 'U')}</div>
                    <div class="sidebar-user-info">
                        <div class="sidebar-user-name">${user?.username || 'User'}</div>
                        <div class="sidebar-user-role">${user?.role || 'USER'}</div>
                    </div>
                    <button class="btn btn-icon btn-outline" id="logout-btn" title="Logout">
                        <span class="material-icons-round" style="font-size:1.1rem">logout</span>
                    </button>
                </div>
            </aside>
            <main class="main-content">
                <header class="top-bar">
                    <div class="top-bar-title">
                        <h1>${pageTitle}</h1>
                        ${subtitle ? `<p>${subtitle}</p>` : ''}
                    </div>
                    <div class="top-bar-actions">
                        <button class="btn btn-icon btn-outline" id="mobile-menu-btn" style="display:none">
                            <span class="material-icons-round">menu</span>
                        </button>
                    </div>
                </header>
                <div class="page-content" id="page-content">${content}</div>
            </main>
        </div>
    `;

    app.querySelectorAll('.nav-item[data-nav]').forEach(item => {
        item.addEventListener('click', () => navigate(item.dataset.nav));
    });

    document.getElementById('logout-btn').addEventListener('click', () => {
        clearAuth();
        navigate('/login');
    });
}
