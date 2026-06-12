import { AccountAPI, TransactionAPI } from '../api.js';
import { renderLayout } from '../layout.js';
import { formatCurrency, formatDateTime, statusBadge, showToast } from '../utils.js';

export async function renderDashboard() {
    renderLayout('Dashboard', 'Welcome back to Neptune Bank', '<div class="page-spinner"><div class="spinner"></div></div>');

    try {
        let accounts = [];
        let transactions = [];

        try { accounts = await AccountAPI.getAll(); } catch {}
        try { transactions = await TransactionAPI.getAll(); } catch {}

        if (!Array.isArray(accounts)) accounts = [];
        if (!Array.isArray(transactions)) transactions = [];

        const totalBalance = accounts.reduce((sum, a) => sum + (parseFloat(a.balance) || 0), 0);
        const recentTxns = transactions.slice(0, 8);
        const successfulTxns = transactions.filter(t => t.transactionStatus === 'SUCCESSFUL').length;

        const content = document.getElementById('page-content');
        content.innerHTML = `
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-card-header">
                        <div>
                            <div class="stat-card-label">Total Balance</div>
                            <div class="stat-card-value">${formatCurrency(totalBalance)}</div>
                        </div>
                        <div class="stat-card-icon blue">
                            <span class="material-icons-round">account_balance_wallet</span>
                        </div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-card-header">
                        <div>
                            <div class="stat-card-label">Accounts</div>
                            <div class="stat-card-value">${accounts.length}</div>
                        </div>
                        <div class="stat-card-icon green">
                            <span class="material-icons-round">account_balance</span>
                        </div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-card-header">
                        <div>
                            <div class="stat-card-label">Transactions</div>
                            <div class="stat-card-value">${transactions.length}</div>
                        </div>
                        <div class="stat-card-icon purple">
                            <span class="material-icons-round">receipt_long</span>
                        </div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-card-header">
                        <div>
                            <div class="stat-card-label">Successful</div>
                            <div class="stat-card-value">${successfulTxns}</div>
                        </div>
                        <div class="stat-card-icon orange">
                            <span class="material-icons-round">check_circle</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="grid-2">
                <div class="card">
                    <div class="card-header">
                        <h2>Your Accounts</h2>
                        <button class="btn btn-sm btn-outline" onclick="location.hash='#/accounts'">View All</button>
                    </div>
                    <div class="card-body-flush">
                        ${accounts.length === 0 ? `
                            <div class="empty-state">
                                <span class="material-icons-round">account_balance</span>
                                <h3>No Accounts</h3>
                                <p>Create your first account to get started.</p>
                            </div>
                        ` : `
                            <table class="data-table">
                                <thead><tr><th>Account</th><th>Type</th><th>Balance</th></tr></thead>
                                <tbody>
                                    ${accounts.slice(0, 5).map(a => `
                                        <tr>
                                            <td style="font-family:'Courier New',monospace;font-size:0.82rem">${a.accountNumber || '—'}</td>
                                            <td><span class="badge badge-info">${(a.accountType || '').replace(/_/g, ' ')}</span></td>
                                            <td class="amount">${formatCurrency(a.balance)}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        `}
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h2>Recent Transactions</h2>
                        <button class="btn btn-sm btn-outline" onclick="location.hash='#/transactions'">View All</button>
                    </div>
                    <div class="card-body-flush">
                        ${recentTxns.length === 0 ? `
                            <div class="empty-state">
                                <span class="material-icons-round">receipt_long</span>
                                <h3>No Transactions</h3>
                                <p>Your transaction history will appear here.</p>
                            </div>
                        ` : `
                            <table class="data-table">
                                <thead><tr><th>Date</th><th>Amount</th><th>Status</th></tr></thead>
                                <tbody>
                                    ${recentTxns.map(t => `
                                        <tr>
                                            <td style="font-size:0.82rem">${formatDateTime(t.transactionDate || t.createdAt)}</td>
                                            <td class="amount">${formatCurrency(t.amount)}</td>
                                            <td>${statusBadge(t.transactionStatus)}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        `}
                    </div>
                </div>
            </div>
        `;
    } catch (err) {
        showToast('Failed to load dashboard data', 'error');
    }
}
