import { TransactionAPI } from '../api.js';
import { renderLayout } from '../layout.js';
import { formatCurrency, showToast } from '../utils.js';

export async function renderTransfer() {
    renderLayout('Fund Transfer', 'Transfer money between accounts', '');

    const content = document.getElementById('page-content');
    content.innerHTML = `
        <div class="transfer-card">
            <div class="card">
                <div class="card-header">
                    <h2>New Transfer</h2>
                    <span class="material-icons-round" style="color:var(--accent)">swap_horiz</span>
                </div>
                <div class="card-body">
                    <div class="transfer-visual" id="transfer-visual" style="display:none">
                        <div class="transfer-account-box">
                            <span class="material-icons-round">arrow_upward</span>
                            <p>From</p>
                            <div class="acc-num" id="vis-source">—</div>
                        </div>
                        <span class="material-icons-round transfer-arrow">arrow_forward</span>
                        <div class="transfer-account-box">
                            <span class="material-icons-round">arrow_downward</span>
                            <p>To</p>
                            <div class="acc-num" id="vis-dest">—</div>
                        </div>
                    </div>

                    <form id="transfer-form">
                        <div class="grid-2">
                            <div class="form-group">
                                <label>Source Account Number</label>
                                <input type="text" class="form-input" id="tf-source" placeholder="e.g. NB4A7F2E3C1D8B" required />
                            </div>
                            <div class="form-group">
                                <label>Destination Account Number</label>
                                <input type="text" class="form-input" id="tf-dest" placeholder="e.g. NB9X2K5M7P3Q1R" required />
                            </div>
                        </div>
                        <div class="form-group">
                            <label>Amount (INR)</label>
                            <input type="number" class="form-input" id="tf-amount" placeholder="Enter amount" min="1" step="1" required style="font-size:1.2rem;font-weight:600" />
                        </div>
                        <div class="grid-2">
                            <div class="form-group">
                                <label>Description</label>
                                <input type="text" class="form-input" id="tf-desc" placeholder="e.g. Rent payment" />
                            </div>
                            <div class="form-group">
                                <label>Remarks</label>
                                <input type="text" class="form-input" id="tf-remarks" placeholder="e.g. June 2026" />
                            </div>
                        </div>

                        <details style="margin-bottom:1.25rem">
                            <summary style="cursor:pointer;font-size:0.85rem;color:var(--text-secondary);padding:0.5rem 0">
                                Bank Details (optional)
                            </summary>
                            <div style="padding-top:0.75rem">
                                <div class="grid-2">
                                    <div class="form-group">
                                        <label>Bank Name</label>
                                        <input type="text" class="form-input" id="tf-bankName" placeholder="Neptune Bank" />
                                    </div>
                                    <div class="form-group">
                                        <label>IFSC Code</label>
                                        <input type="text" class="form-input" id="tf-ifsc" placeholder="NPTB0001234" />
                                    </div>
                                </div>
                                <div class="grid-3">
                                    <div class="form-group">
                                        <label>Branch Name</label>
                                        <input type="text" class="form-input" id="tf-branchName" placeholder="Main Branch" />
                                    </div>
                                    <div class="form-group">
                                        <label>City</label>
                                        <input type="text" class="form-input" id="tf-city" placeholder="Mumbai" />
                                    </div>
                                    <div class="form-group">
                                        <label>State</label>
                                        <input type="text" class="form-input" id="tf-state" placeholder="Maharashtra" />
                                    </div>
                                </div>
                            </div>
                        </details>

                        <button type="submit" class="btn btn-primary btn-full" id="tf-submit" style="padding:1rem;font-size:1rem">
                            <span class="material-icons-round">send</span>
                            Transfer Now
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <div id="transfer-result" style="margin-top:1.5rem"></div>
    `;

    const sourceInput = document.getElementById('tf-source');
    const destInput = document.getElementById('tf-dest');
    const visual = document.getElementById('transfer-visual');

    function updateVisual() {
        const s = sourceInput.value.trim();
        const d = destInput.value.trim();
        if (s && d) {
            visual.style.display = 'flex';
            document.getElementById('vis-source').textContent = s;
            document.getElementById('vis-dest').textContent = d;
        } else {
            visual.style.display = 'none';
        }
    }

    sourceInput.addEventListener('input', updateVisual);
    destInput.addEventListener('input', updateVisual);

    document.getElementById('transfer-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('tf-submit');
        const source = sourceInput.value.trim();
        const dest = destInput.value.trim();
        const amount = parseInt(document.getElementById('tf-amount').value);

        if (!source || !dest || !amount) {
            showToast('Please fill in all required fields', 'error');
            return;
        }

        if (source === dest) {
            showToast('Source and destination must be different', 'error');
            return;
        }

        if (amount <= 0) {
            showToast('Amount must be greater than 0', 'error');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Processing...';

        try {
            const payload = {
                sourceAccountNumber: source,
                destinationAccountNumber: dest,
                amount,
                description: document.getElementById('tf-desc').value.trim() || null,
                remarks: document.getElementById('tf-remarks').value.trim() || null,
                sourceOrDestinationBank: {
                    bankName: document.getElementById('tf-bankName').value.trim() || 'Neptune Bank',
                    bankIFSCCode: document.getElementById('tf-ifsc').value.trim() || null,
                    bankBranchName: document.getElementById('tf-branchName').value.trim() || null,
                    bankBranchCity: document.getElementById('tf-city').value.trim() || null,
                    bankBranchState: document.getElementById('tf-state').value.trim() || null,
                    bankBranchCountry: 'India',
                    bankBranchZipCode: null
                }
            };

            const res = await TransactionAPI.transfer(payload);
            showToast('Transfer completed successfully!', 'success');

            document.getElementById('transfer-result').innerHTML = `
                <div class="card" style="border-color:rgba(34,197,94,0.3)">
                    <div class="card-body" style="text-align:center">
                        <span class="material-icons-round" style="font-size:3rem;color:var(--success);margin-bottom:0.75rem">check_circle</span>
                        <h2 style="margin-bottom:0.5rem">Transfer Successful</h2>
                        <p style="color:var(--text-secondary);margin-bottom:1rem">${formatCurrency(amount)} has been transferred.</p>
                        <div class="detail-grid" style="max-width:400px;margin:0 auto">
                            <div class="detail-item">
                                <label>Transaction ID</label>
                                <span style="font-size:0.75rem;font-family:'Courier New',monospace;word-break:break-all">${res.transactionId || '—'}</span>
                            </div>
                            <div class="detail-item">
                                <label>Status</label>
                                <span style="color:var(--success);font-weight:600">${res.status || 'Success'}</span>
                            </div>
                        </div>
                        <button class="btn btn-outline" style="margin-top:1.25rem" onclick="location.hash='#/transactions'">
                            View Transaction History
                        </button>
                    </div>
                </div>
            `;

            document.getElementById('transfer-form').reset();
            visual.style.display = 'none';
        } catch (err) {
            showToast(err.message || 'Transfer failed', 'error');
            document.getElementById('transfer-result').innerHTML = `
                <div class="card" style="border-color:rgba(239,68,68,0.3)">
                    <div class="card-body" style="text-align:center">
                        <span class="material-icons-round" style="font-size:3rem;color:var(--danger);margin-bottom:0.75rem">cancel</span>
                        <h2 style="margin-bottom:0.5rem">Transfer Failed</h2>
                        <p style="color:var(--text-secondary)">${err.message}</p>
                    </div>
                </div>
            `;
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<span class="material-icons-round">send</span> Transfer Now';
        }
    });
}
