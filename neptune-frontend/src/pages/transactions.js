import { TransactionAPI } from '../api.js';
import { renderLayout } from '../layout.js';
import { formatCurrency, formatDateTime, statusBadge, showToast, showModal } from '../utils.js';

export async function renderTransactions(params) {
    renderLayout('Transaction History', 'View all your transactions', '<div class="page-spinner"><div class="spinner"></div></div>');

    const accountId = params?.get('accountId');
    await loadTransactions(accountId);
}

async function loadTransactions(accountId) {
    const content = document.getElementById('page-content');
    try {
        let transactions = [];
        try {
            if (accountId) {
                transactions = await TransactionAPI.getByAccount(accountId);
            } else {
                transactions = await TransactionAPI.getAll();
            }
        } catch {}
        if (!Array.isArray(transactions)) transactions = [];

        content.innerHTML = `
            <div class="card">
                <div class="card-header">
                    <h2>${accountId ? `Account #${accountId} Transactions` : 'All Transactions'} (${transactions.length})</h2>
                    <div style="display:flex;gap:0.75rem">
                        ${accountId ? `<button class="btn btn-sm btn-outline" onclick="location.hash='#/transactions'">Show All</button>` : ''}
                    </div>
                </div>
                <div class="card-body-flush">
                    ${transactions.length === 0 ? `
                        <div class="empty-state">
                            <span class="material-icons-round">receipt_long</span>
                            <h3>No Transactions Found</h3>
                            <p>${accountId ? 'No transactions for this account.' : 'Your transaction history will appear here.'}</p>
                        </div>
                    ` : `
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Transaction ID</th>
                                    <th>Date</th>
                                    <th>Type</th>
                                    <th>Medium</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${transactions.map(t => `
                                    <tr>
                                        <td style="font-family:'Courier New',monospace;font-size:0.75rem;max-width:150px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="${t.transactionId || ''}">${(t.transactionId || '—').slice(0, 20)}...</td>
                                        <td style="font-size:0.82rem;white-space:nowrap">${formatDateTime(t.transactionDate || t.createdAt)}</td>
                                        <td><span class="badge badge-info">${(t.transactionType || '').replace(/_/g, ' ')}</span></td>
                                        <td style="font-size:0.82rem">${(t.transactionMedium || '').replace(/_/g, ' ')}</td>
                                        <td class="amount">${formatCurrency(t.amount)}</td>
                                        <td>${statusBadge(t.transactionStatus)}</td>
                                        <td>
                                            <button class="btn btn-sm btn-outline txn-detail-btn" data-id="${t.id}">
                                                <span class="material-icons-round" style="font-size:0.9rem">visibility</span>
                                            </button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    `}
                </div>
            </div>
        `;

        content.querySelectorAll('.txn-detail-btn').forEach(btn => {
            btn.addEventListener('click', () => showTxnDetail(btn.dataset.id, transactions));
        });

    } catch (err) {
        content.innerHTML = `<div class="card"><div class="empty-state"><span class="material-icons-round">error</span><h3>Failed to load transactions</h3><p>${err.message}</p></div></div>`;
    }
}

function showTxnDetail(id, transactions) {
    const t = transactions.find(tx => String(tx.id) === String(id));
    if (!t) return;

    const body = `
        <div class="detail-grid">
            <div class="detail-item">
                <label>Transaction ID</label>
                <span style="font-size:0.78rem;font-family:'Courier New',monospace;word-break:break-all">${t.transactionId || '—'}</span>
            </div>
            <div class="detail-item">
                <label>Date</label>
                <span>${formatDateTime(t.transactionDate || t.createdAt)}</span>
            </div>
            <div class="detail-item">
                <label>Type</label>
                <span>${(t.transactionType || '').replace(/_/g, ' ')}</span>
            </div>
            <div class="detail-item">
                <label>Medium</label>
                <span>${(t.transactionMedium || '').replace(/_/g, ' ')}</span>
            </div>
            <div class="detail-item">
                <label>Mode</label>
                <span>${(t.modeOfTransaction || '').replace(/_/g, ' ')}</span>
            </div>
            <div class="detail-item">
                <label>Amount</label>
                <span class="amount" style="font-size:1.1rem">${formatCurrency(t.amount)}</span>
            </div>
            <div class="detail-item">
                <label>Source Account</label>
                <span>${t.sourceAccount || '—'}</span>
            </div>
            <div class="detail-item">
                <label>Destination Account</label>
                <span>${t.destinationAccount || '—'}</span>
            </div>
            <div class="detail-item">
                <label>Status</label>
                <span>${statusBadge(t.transactionStatus)}</span>
            </div>
            <div class="detail-item">
                <label>Description</label>
                <span>${t.description || '—'}</span>
            </div>
            <div class="detail-item">
                <label>Remarks</label>
                <span>${t.remarks || '—'}</span>
            </div>
        </div>
    `;

    showModal('Transaction Details', body);
}
