const BASE_URLS = {
    auth: 'http://localhost:8086',
    user: 'http://localhost:8080',
    otp: 'http://localhost:8082',
    account: 'http://localhost:8083',
    transaction: 'http://localhost:8084'
};

function getToken() {
    return localStorage.getItem('neptune_token');
}

function getUser() {
    const u = localStorage.getItem('neptune_user');
    return u ? JSON.parse(u) : null;
}

function setAuth(token, user) {
    localStorage.setItem('neptune_token', token);
    localStorage.setItem('neptune_user', JSON.stringify(user));
}

function clearAuth() {
    localStorage.removeItem('neptune_token');
    localStorage.removeItem('neptune_user');
}

function isLoggedIn() {
    return !!getToken();
}

async function request(baseKey, path, options = {}) {
    const url = `${BASE_URLS[baseKey]}${path}`;
    const headers = { ...options.headers };

    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = headers['Content-Type'] || 'application/json';
    }

    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(url, { ...options, headers });

    if (res.status === 401 || res.status === 403) {
        clearAuth();
        window.location.hash = '#/login';
        throw new Error('Session expired');
    }

    const text = await res.text();
    let data;
    try {
        data = JSON.parse(text);
    } catch {
        data = text;
    }

    if (!res.ok) {
        const msg = data?.message || data?.error || `Request failed (${res.status})`;
        throw new Error(msg);
    }

    return data;
}

export const AuthAPI = {
    login(username, password, mode = 'jwt') {
        return request('auth', '/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password, mode })
        });
    },

    register(username, password, role = 'USER') {
        return request('auth', '/auth/add-user', {
            method: 'POST',
            body: JSON.stringify({ username, password, role })
        });
    },

    validate() {
        return request('auth', '/auth/validate');
    }
};

export const UserAPI = {
    getById(id) {
        return request('user', `/auth/user/${id}`);
    },

    getAll() {
        return request('user', '/auth/users');
    },

    checkExists(id) {
        return request('user', `/auth/user/exists/${id}`);
    },

    update(id, data) {
        return request('user', `/auth/user/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(id) {
        return request('user', `/auth/user/${id}`, { method: 'DELETE' });
    }
};

export const AccountAPI = {
    create(data) {
        return request('account', '/api/accounts/create', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    getById(id) {
        return request('account', `/api/accounts/get/${id}`);
    },

    getAll() {
        return request('account', '/api/accounts/get/all');
    },

    getByUserId(userId) {
        return request('account', `/api/accounts/user/${userId}`);
    },

    getByNumber(accountNumber) {
        return request('account', `/api/accounts/number/${accountNumber}`);
    },

    update(id, data) {
        return request('account', `/api/accounts/update/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(id) {
        return request('account', `/api/accounts/delete/${id}`, { method: 'DELETE' });
    }
};

export const TransactionAPI = {
    create(data) {
        return request('transaction', '/transactions/create', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    transfer(data) {
        return request('transaction', '/transactions/transfer', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    getById(id) {
        return request('transaction', `/transactions/${id}`);
    },

    getByAccount(accountId) {
        return request('transaction', `/transactions/account/${accountId}`);
    },

    getAll() {
        return request('transaction', '/transactions/all');
    }
};

export const OtpAPI = {
    send(phone, email) {
        return request('otp', '/api/otp/send', {
            method: 'POST',
            body: JSON.stringify({ phone, email })
        });
    },

    verify(identifier, otp) {
        return request('otp', '/api/otp/verify', {
            method: 'POST',
            body: JSON.stringify({ identifier, otp })
        });
    }
};

export { getToken, getUser, setAuth, clearAuth, isLoggedIn };
