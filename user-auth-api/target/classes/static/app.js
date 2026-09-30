// Current Auth State
let currentToken = localStorage.getItem('auth_jwt_token') || null;
let currentUser = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    checkHealth();
    restoreSession();
});

// Switch active tab
function switchTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

    const activeBtn = Array.from(document.querySelectorAll('.tab-btn')).find(b =>
        b.getAttribute('onclick')?.includes(tabId)
    );
    if (activeBtn) activeBtn.classList.add('active');

    const tab = document.getElementById(tabId);
    if (tab) tab.classList.add('active');
}

// Banner alerts
function showAlert(message, type = 'success') {
    const banner = document.getElementById('alertBanner');
    banner.className = `alert-banner ${type}`;
    banner.textContent = message;
    banner.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    setTimeout(() => {
        banner.className = 'alert-banner hidden';
    }, 6000);
}

// Health Check
async function checkHealth() {
    const indicator = document.getElementById('apiStatus');
    indicator.textContent = 'Pinging...';
    try {
        const start = performance.now();
        const res = await fetch('/api/auth/health');
        const latency = Math.round(performance.now() - start);
        const data = await res.json();

        if (res.ok) {
            indicator.className = 'status-indicator online';
            indicator.textContent = `API Online (${latency}ms)`;
        } else {
            indicator.className = 'status-indicator offline';
            indicator.textContent = 'API Error';
        }
    } catch (e) {
        indicator.className = 'status-indicator offline';
        indicator.textContent = 'API Offline';
    }
}

// Session Management
function restoreSession() {
    const storedUser = localStorage.getItem('auth_user');
    if (currentToken && storedUser) {
        try {
            currentUser = JSON.parse(storedUser);
            renderSessionUI();
        } catch (e) {
            clearToken();
        }
    } else {
        updateJwtDisplay();
    }
}

function renderSessionUI() {
    const sessionBox = document.getElementById('sessionBox');
    if (currentToken && currentUser) {
        sessionBox.classList.remove('hidden');
        document.getElementById('sessionUsername').textContent = currentUser.username || currentUser.fullName;
        document.getElementById('sessionRole').textContent = currentUser.role || 'ROLE_USER';
        document.getElementById('sessionExpiry').textContent = 'Valid for 24 Hours';
    } else {
        sessionBox.classList.add('hidden');
    }
    updateJwtDisplay();
}

function updateJwtDisplay() {
    const display = document.getElementById('jwtTokenDisplay');
    if (currentToken) {
        display.textContent = currentToken;
    } else {
        display.textContent = 'No token stored. Log in or register to acquire a JWT.';
    }
}

function clearToken() {
    currentToken = null;
    currentUser = null;
    localStorage.removeItem('auth_jwt_token');
    localStorage.removeItem('auth_user');
    renderSessionUI();
    document.getElementById('profileResult').classList.add('hidden');
    showAlert('Logged out. Token cleared from local session.', 'success');
}

// Clear field errors
function clearFieldErrors() {
    document.querySelectorAll('.field-error').forEach(el => el.textContent = '');
    document.querySelectorAll('input').forEach(el => el.classList.remove('is-invalid'));
}

// Render validation errors on form
function renderFieldErrors(validationErrors) {
    if (!validationErrors) return;
    for (const [field, message] of Object.entries(validationErrors)) {
        const errorEl = document.getElementById(`err-${field}`);
        if (errorEl) {
            errorEl.textContent = message;
        }
        const inputEl = document.getElementById(
            field === 'password' ? 'regPassword' :
            field === 'fullName' ? 'regFullName' :
            field === 'username' ? 'regUsername' :
            field === 'email' ? 'regEmail' : field
        );
        if (inputEl) {
            inputEl.classList.add('is-invalid');
        }
    }
}

// Logger helper for Inspector
function logTraffic(method, url, requestBody, status, responseData) {
    const metaTag = document.getElementById('requestMeta');
    metaTag.textContent = `${method} ${url}`;

    const requestLog = document.getElementById('requestLog');
    if (requestBody) {
        requestLog.textContent = JSON.stringify(requestBody, null, 2);
    } else {
        requestLog.textContent = `// Headers: Authorization Bearer ...\n// No request body`;
    }

    const responseLog = document.getElementById('responseLog');
    responseLog.textContent = JSON.stringify(responseData, null, 2);

    const statusPill = document.getElementById('responseStatus');
    statusPill.className = `status-pill status-${status}`;
    statusPill.textContent = `${status} ${getStatusText(status)}`;
}

function getStatusText(status) {
    switch (status) {
        case 200: return 'OK';
        case 201: return 'CREATED';
        case 400: return 'BAD REQUEST';
        case 401: return 'UNAUTHORIZED';
        case 403: return 'FORBIDDEN';
        case 404: return 'NOT FOUND';
        case 409: return 'CONFLICT';
        case 500: return 'SERVER ERROR';
        default: return '';
    }
}

// Sample User autofill
function fillSampleUser() {
    const rand = Math.floor(1000 + Math.random() * 9000);
    document.getElementById('regFullName').value = 'Alex Morgan';
    document.getElementById('regUsername').value = `alex${rand}`;
    document.getElementById('regEmail').value = `alex${rand}@example.com`;
    document.getElementById('regPassword').value = 'Secret@123';
    clearFieldErrors();
}

// 1. Handle Registration
async function handleRegister(e) {
    e.preventDefault();
    clearFieldErrors();

    const payload = {
        fullName: document.getElementById('regFullName').value.trim(),
        username: document.getElementById('regUsername').value.trim(),
        email: document.getElementById('regEmail').value.trim(),
        password: document.getElementById('regPassword').value,
        role: document.getElementById('regRole').value
    };

    const submitBtn = document.getElementById('btnRegister');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registering...';

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        logTraffic('POST', '/api/auth/register', payload, response.status, data);

        if (response.ok) {
            showAlert(`Success: ${data.message}`, 'success');
            // Auto-fill login form for convenience
            document.getElementById('loginIdentifier').value = payload.username;
            document.getElementById('loginPassword').value = payload.password;
            // Switch to login tab
            setTimeout(() => switchTab('loginTab'), 1200);
        } else {
            showAlert(`Error (${response.status}): ${data.message || 'Registration failed'}`, 'error');
            if (data.validationErrors) {
                renderFieldErrors(data.validationErrors);
            }
        }
    } catch (err) {
        showAlert('Network error communicating with server: ' + err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Account';
    }
}

// 2. Handle Login
async function handleLogin(e) {
    e.preventDefault();
    clearFieldErrors();

    const payload = {
        usernameOrEmail: document.getElementById('loginIdentifier').value.trim(),
        password: document.getElementById('loginPassword').value
    };

    const submitBtn = document.getElementById('btnLogin');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Authenticating...';

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        logTraffic('POST', '/api/auth/login', payload, response.status, data);

        if (response.ok && data.data) {
            currentToken = data.data.accessToken;
            currentUser = data.data.user;
            localStorage.setItem('auth_jwt_token', currentToken);
            localStorage.setItem('auth_user', JSON.stringify(currentUser));
            renderSessionUI();
            showAlert(`Logged in successfully! Welcome, ${currentUser.fullName}`, 'success');
        } else {
            showAlert(`Login Failed (${response.status}): ${data.message || 'Invalid credentials'}`, 'error');
            if (data.validationErrors) {
                renderFieldErrors(data.validationErrors);
            }
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Authenticate & Get JWT';
    }
}

// 3. Fetch Current User Profile (/me) with Token
async function fetchCurrentUser() {
    if (!currentToken) {
        showAlert('No JWT token found! Please log in first to get an access token.', 'error');
        switchTab('loginTab');
        return;
    }

    try {
        const response = await fetch('/api/auth/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        const data = await response.json();
        logTraffic('GET', '/api/auth/me', null, response.status, data);

        if (response.ok && data.data) {
            const user = data.data;
            const profileCard = document.getElementById('profileResult');
            profileCard.classList.remove('hidden');

            document.getElementById('profFullName').textContent = user.fullName;
            document.getElementById('profUsername').textContent = user.username;
            document.getElementById('profEmail').textContent = user.email;
            document.getElementById('profRole').textContent = user.role;

            const initials = user.fullName
                .split(' ')
                .map(n => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase();
            document.getElementById('profileInitials').textContent = initials || 'U';

            showAlert('Profile loaded from JWT authentication context!', 'success');
        } else {
            showAlert(`Failed to fetch profile: ${data.message}`, 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    }
}

// Test /me without token to demonstrate 401 Unauthorized
async function fetchCurrentUserWithoutToken() {
    try {
        const response = await fetch('/api/auth/me', {
            method: 'GET'
        });

        const data = await response.json();
        logTraffic('GET', '/api/auth/me (No Token)', null, response.status, data);
        showAlert(`Received expected HTTP 401 Unauthorized response`, 'error');
    } catch (err) {
        showAlert('Request error: ' + err.message, 'error');
    }
}

// 4. Fetch All Users (/api/users)
async function fetchAllUsers() {
    if (!currentToken) {
        showAlert('Token required! Please log in first to access /api/users.', 'error');
        switchTab('loginTab');
        return;
    }

    try {
        const response = await fetch('/api/users', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        const data = await response.json();
        logTraffic('GET', '/api/users', null, response.status, data);

        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = '';

        if (response.ok && data.data && data.data.length > 0) {
            data.data.forEach(user => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><code>#${user.id}</code></td>
                    <td><strong>${escapeHtml(user.fullName)}</strong></td>
                    <td><code>@${escapeHtml(user.username)}</code></td>
                    <td>${escapeHtml(user.email)}</td>
                    <td><span class="badge-role">${user.role}</span></td>
                    <td><span class="status-indicator online">Active</span></td>
                `;
                tbody.appendChild(tr);
            });
            showAlert(`Retrieved ${data.data.length} users from database.`, 'success');
        } else if (response.ok) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">No users registered yet.</td></tr>';
        } else {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center" style="color:var(--danger)">Error: ${escapeHtml(data.message)}</td></tr>`;
            showAlert(`Access error (${response.status}): ${data.message}`, 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    }
}

// Trigger Validation Error intentional demo
async function triggerValidationError() {
    clearFieldErrors();
    const badPayload = {
        fullName: "", // Blank!
        username: "ab", // Too short (min 3)!
        email: "not-an-email", // Invalid email format!
        password: "123" // Too short (min 6)!
    };

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(badPayload)
        });

        const data = await response.json();
        logTraffic('POST', '/api/auth/register (Invalid payload)', badPayload, response.status, data);

        if (data.validationErrors) {
            renderFieldErrors(data.validationErrors);
            showAlert('Demonstrated Bean Validation: 400 Bad Request with field error map.', 'error');
        }
    } catch (e) {
        showAlert(e.message, 'error');
    }
}

// Trigger Bad Credentials demo
async function triggerBadCredentials() {
    clearFieldErrors();
    const badLogin = {
        usernameOrEmail: "non_existent_user",
        password: "wrong_password_999"
    };

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(badLogin)
        });

        const data = await response.json();
        logTraffic('POST', '/api/auth/login (Bad Credentials)', badLogin, response.status, data);
        showAlert(`Demonstrated Bad Credentials: 401 Unauthorized structured response`, 'error');
    } catch (e) {
        showAlert(e.message, 'error');
    }
}

// Console & Clipboard tools
function clearConsole() {
    document.getElementById('requestMeta').textContent = 'None';
    document.getElementById('requestLog').textContent = '// Console cleared';
    document.getElementById('responseLog').textContent = '// Waiting for request...';
    document.getElementById('responseStatus').className = 'status-pill status-ready';
    document.getElementById('responseStatus').textContent = 'IDLE';
}

function copyResponse() {
    const text = document.getElementById('responseLog').textContent;
    navigator.clipboard.writeText(text).then(() => {
        showAlert('Response JSON copied to clipboard!', 'success');
    });
}

function copyToken() {
    if (!currentToken) {
        showAlert('No token to copy!', 'error');
        return;
    }
    navigator.clipboard.writeText(currentToken).then(() => {
        showAlert('JWT Bearer token copied to clipboard!', 'success');
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
