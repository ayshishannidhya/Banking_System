import { AccountAPI } from '../api.js';
import { renderLayout } from '../layout.js';
import { formatCurrency, showToast, showModal, closeModal } from '../utils.js';

export async function renderAccounts() {
    renderLayout('Accounts', 'Manage your bank accounts', '<div class="page-spinner"><div class="spinner"></div></div>');
    await loadAccounts();
}

async function loadAccounts() {
    const content = document.getElementById('page-content');
    try {
        let accounts = [];
        try { accounts = await AccountAPI.getAll(); } catch {}
        if (!Array.isArray(accounts)) accounts = [];

        content.innerHTML = `
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1.5rem">
                <h2 style="font-size:1.1rem;font-weight:600">${accounts.length} Account${accounts.length !== 1 ? 's' : ''}</h2>
                <button class="btn btn-primary btn-sm" id="create-account-btn">
                    <span class="material-icons-round" style="font-size:1.1rem">add</span>
                    New Account
                </button>
            </div>

            ${accounts.length === 0 ? `
                <div class="card">
                    <div class="empty-state">
                        <span class="material-icons-round">account_balance</span>
                        <h3>No Accounts Yet</h3>
                        <p>Create your first bank account to start banking.</p>
                        <button class="btn btn-primary" style="margin-top:1rem" id="create-account-empty-btn">
                            <span class="material-icons-round" style="font-size:1.1rem">add</span>
                            Create Account
                        </button>
                    </div>
                </div>
            ` : `
                <div class="accounts-grid">
                    ${accounts.map(a => {
                        const type = (a.accountType || 'savings').toLowerCase();
                        return `
                            <div class="account-card ${type}">
                                <div class="account-card-type">
                                    <span class="material-icons-round" style="font-size:1rem">
                                        ${type === 'savings' ? 'savings' : type === 'current' ? 'business' : 'lock'}
                                    </span>
                                    ${(a.accountType || '').replace(/_/g, ' ')}
                                </div>
                                <div class="account-card-number">${a.accountNumber || '—'}</div>
                                <div class="account-card-balance">${formatCurrency(a.balance)}</div>
                                <div class="account-card-footer">
                                    <span style="font-size:0.78rem;color:var(--text-muted)">
                                        ${a.modeOfOperation ? a.modeOfOperation.replace(/_/g, ' ') : 'Single'}
                                    </span>
                                    <div style="display:flex;gap:0.5rem">
                                        <button class="btn btn-sm btn-outline view-txn-btn" data-acc="${a.accountId}">
                                            <span class="material-icons-round" style="font-size:0.9rem">receipt_long</span>
                                            History
                                        </button>
                                    </div>
                                </div>
                            </div>
                        `;
                    }).join('')}
                </div>
            `}
        `;

        const createBtn = document.getElementById('create-account-btn');
        if (createBtn) createBtn.addEventListener('click', showCreateModal);

        const emptyBtn = document.getElementById('create-account-empty-btn');
        if (emptyBtn) emptyBtn.addEventListener('click', showCreateModal);

        content.querySelectorAll('.view-txn-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                window.location.hash = `#/transactions?accountId=${btn.dataset.acc}`;
            });
        });

    } catch (err) {
        content.innerHTML = `<div class="card"><div class="empty-state"><span class="material-icons-round">error</span><h3>Failed to load accounts</h3><p>${err.message}</p></div></div>`;
    }
}

function showCreateModal() {
    const body = `
        <form id="create-account-form">
            <div class="form-group">
                <label>User ID</label>
                <input type="number" class="form-input" id="ca-userId" placeholder="Enter user ID" required />
            </div>
            <div class="form-group">
                <label>Account Type</label>
                <select class="form-select" id="ca-type">
                    <option value="SAVINGS">Savings</option>
                    <option value="CURRENT">Current</option>
                    <option value="FIXED_DEPOSIT">Fixed Deposit</option>
                </select>
            </div>
            <div class="form-group">
                <label>Mode of Operation</label>
                <select class="form-select" id="ca-mode">
                    <option value="SINGLE">Single</option>
                    <option value="JOINT">Joint</option>
                    <option value="EITHER_OR_SURVIVOR">Either or Survivor</option>
                </select>
            </div>
            <div class="form-group">
                <label>Branch ID</label>
                <input type="number" class="form-input" id="ca-branch" placeholder="Enter branch ID" required />
            </div>
            <div class="form-group">
                <label>Initial Balance</label>
                <input type="number" class="form-input" id="ca-balance" placeholder="0.00" step="0.01" value="0" />
            </div>
        </form>
    `;

    const footer = `
        <button class="btn btn-outline" onclick="document.querySelector('.modal-overlay').remove()">Cancel</button>
        <button class="btn btn-primary" id="ca-submit-btn">Create Account</button>
    `;

    showModal('Create New Account', body, footer);

    document.getElementById('ca-submit-btn').addEventListener('click', async () => {
        const btn = document.getElementById('ca-submit-btn');
        const data = {
            userId: parseInt(document.getElementById('ca-userId').value),
            accountType: document.getElementById('ca-type').value,
            modeOfOperation: document.getElementById('ca-mode').value,
            branchId: parseInt(document.getElementById('ca-branch').value),
            balance: parseFloat(document.getElementById('ca-balance').value) || 0
        };

        if (!data.userId || !data.branchId) {
            showToast('Please fill in all required fields', 'error');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span>';

        try {
            await AccountAPI.create(data);
            showToast('Account created successfully', 'success');
            closeModal();
            await loadAccounts();
        } catch (err) {
            showToast(err.message || 'Failed to create account', 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Create Account';
        }
    });
}
