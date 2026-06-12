import { UserAPI, getUser } from '../api.js';
import { renderLayout } from '../layout.js';
import { formatDate, showToast, getInitials } from '../utils.js';

export async function renderProfile() {
    renderLayout('Profile', 'View and manage user profiles', '<div class="page-spinner"><div class="spinner"></div></div>');

    const content = document.getElementById('page-content');

    content.innerHTML = `
        <div class="card" style="margin-bottom:1.5rem">
            <div class="card-header">
                <h2>Lookup User Profile</h2>
            </div>
            <div class="card-body">
                <div style="display:flex;gap:0.75rem;align-items:end">
                    <div class="form-group" style="flex:1;margin-bottom:0">
                        <label>User ID</label>
                        <input type="number" class="form-input" id="profile-user-id" placeholder="Enter user ID" />
                    </div>
                    <button class="btn btn-primary" id="profile-lookup-btn" style="height:42px">
                        <span class="material-icons-round" style="font-size:1.1rem">search</span>
                        Search
                    </button>
                    <button class="btn btn-outline" id="profile-all-btn" style="height:42px">
                        All Users
                    </button>
                </div>
            </div>
        </div>
        <div id="profile-result"></div>
    `;

    document.getElementById('profile-lookup-btn').addEventListener('click', async () => {
        const id = document.getElementById('profile-user-id').value.trim();
        if (!id) { showToast('Enter a user ID', 'error'); return; }
        await loadUserProfile(id);
    });

    document.getElementById('profile-all-btn').addEventListener('click', loadAllUsers);
}

async function loadUserProfile(id) {
    const result = document.getElementById('profile-result');
    result.innerHTML = '<div class="page-spinner"><div class="spinner"></div></div>';

    try {
        const user = await UserAPI.getById(id);

        const fields = [
            { label: 'First Name', value: user.firstName },
            { label: 'Middle Name', value: user.middleName },
            { label: 'Last Name', value: user.lastName },
            { label: 'Date of Birth', value: formatDate(user.dateOfBirth) },
            { label: 'Gender', value: user.gender },
            { label: 'Father\'s Name', value: user.fatherName },
            { label: 'Mother\'s Name', value: user.motherName },
            { label: 'Marital Status', value: user.maritalStatus },
            { label: 'Occupation', value: user.occupation },
            { label: 'Salary', value: user.salary },
            { label: 'Citizenship', value: user.citizen },
            { label: 'Category', value: user.category },
            { label: 'Religion', value: user.religion },
        ].filter(f => f.value);

        const contact = user.contactDetails || {};
        const contactFields = [
            { label: 'Phone', value: contact.phone1 },
            { label: 'Alt Phone', value: contact.phone2 },
            { label: 'Email', value: contact.email },
            { label: 'City', value: contact.comCity },
            { label: 'State', value: contact.comState },
            { label: 'ZIP', value: contact.comZip },
            { label: 'Country', value: contact.comCountry },
        ].filter(f => f.value);

        const nominee = user.nominee || {};
        const nomineeFields = [
            { label: 'Name', value: nominee.nomineeName },
            { label: 'Relationship', value: nominee.nomineeRelationship },
            { label: 'Mobile', value: nominee.nomineeMobileNo },
            { label: 'Email', value: nominee.nomineeEmail },
        ].filter(f => f.value);

        const fullName = [user.firstName, user.middleName, user.lastName].filter(Boolean).join(' ');

        result.innerHTML = `
            <div class="card" style="margin-bottom:1.5rem">
                <div class="card-body">
                    <div class="profile-header">
                        <div class="profile-avatar">${getInitials(fullName || 'U')}</div>
                        <div class="profile-info">
                            <h2>${fullName || 'Unknown User'}</h2>
                            <p>User ID: ${user.userid || id} &bull; ${user.isActive ? '<span style="color:var(--success)">Active</span>' : '<span style="color:var(--danger)">Inactive</span>'}</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="grid-2" style="margin-bottom:1.5rem">
                <div class="card">
                    <div class="card-header"><h2>Personal Information</h2></div>
                    <div class="card-body">
                        <div class="detail-grid">
                            ${fields.map(f => `<div class="detail-item"><label>${f.label}</label><span>${f.value}</span></div>`).join('')}
                        </div>
                    </div>
                </div>
                <div class="card">
                    <div class="card-header"><h2>Contact Details</h2></div>
                    <div class="card-body">
                        <div class="detail-grid">
                            ${contactFields.length > 0 ? contactFields.map(f => `<div class="detail-item"><label>${f.label}</label><span>${f.value}</span></div>`).join('') : '<p style="color:var(--text-muted)">No contact details available.</p>'}
                        </div>
                    </div>
                </div>
            </div>

            ${nomineeFields.length > 0 ? `
                <div class="card">
                    <div class="card-header"><h2>Nominee Details</h2></div>
                    <div class="card-body">
                        <div class="detail-grid">
                            ${nomineeFields.map(f => `<div class="detail-item"><label>${f.label}</label><span>${f.value}</span></div>`).join('')}
                        </div>
                    </div>
                </div>
            ` : ''}
        `;
    } catch (err) {
        result.innerHTML = `
            <div class="card">
                <div class="empty-state">
                    <span class="material-icons-round">person_off</span>
                    <h3>User Not Found</h3>
                    <p>${err.message}</p>
                </div>
            </div>
        `;
    }
}

async function loadAllUsers() {
    const result = document.getElementById('profile-result');
    result.innerHTML = '<div class="page-spinner"><div class="spinner"></div></div>';

    try {
        let users = await UserAPI.getAll();
        if (!Array.isArray(users)) users = [];

        result.innerHTML = `
            <div class="card">
                <div class="card-header">
                    <h2>All Users (${users.length})</h2>
                </div>
                <div class="card-body-flush">
                    ${users.length === 0 ? `
                        <div class="empty-state">
                            <span class="material-icons-round">group_off</span>
                            <h3>No Users Found</h3>
                            <p>No registered users in the system.</p>
                        </div>
                    ` : `
                        <table class="data-table">
                            <thead><tr><th>ID</th><th>Name</th><th>Gender</th><th>Occupation</th><th>Status</th><th>Action</th></tr></thead>
                            <tbody>
                                ${users.map(u => {
                                    const name = [u.firstName, u.lastName].filter(Boolean).join(' ') || '—';
                                    return `
                                        <tr>
                                            <td>${u.userid}</td>
                                            <td style="font-weight:500">${name}</td>
                                            <td>${u.gender || '—'}</td>
                                            <td>${u.occupation || '—'}</td>
                                            <td>${u.isActive ? '<span class="badge badge-success"><span class="badge-dot"></span>Active</span>' : '<span class="badge badge-danger"><span class="badge-dot"></span>Inactive</span>'}</td>
                                            <td>
                                                <button class="btn btn-sm btn-outline user-view-btn" data-id="${u.userid}">
                                                    <span class="material-icons-round" style="font-size:0.9rem">visibility</span>
                                                </button>
                                            </td>
                                        </tr>
                                    `;
                                }).join('')}
                            </tbody>
                        </table>
                    `}
                </div>
            </div>
        `;

        result.querySelectorAll('.user-view-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.getElementById('profile-user-id').value = btn.dataset.id;
                loadUserProfile(btn.dataset.id);
            });
        });
    } catch (err) {
        result.innerHTML = `<div class="card"><div class="empty-state"><span class="material-icons-round">error</span><h3>Failed to load users</h3><p>${err.message}</p></div></div>`;
    }
}
