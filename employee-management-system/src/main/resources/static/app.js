/**
 * StaffPulse - Employee Management System
 * Frontend Controller & API Integration
 */

const API_BASE_URL = '/api/employees';

// Application State
const state = {
    employees: [],
    stats: null,
    searchQuery: '',
    selectedDepartment: 'ALL',
    selectedStatus: 'ALL',
    currentEmployee: null,
    isSubmitting: false
};

// DOM Elements
const elements = {
    // API Status
    apiStatusBadge: document.getElementById('apiStatusBadge'),
    apiStatusText: document.getElementById('apiStatusText'),

    // KPI Elements
    kpiTotal: document.getElementById('kpiTotal'),
    kpiActive: document.getElementById('kpiActive'),
    kpiActivePercent: document.getElementById('kpiActivePercent'),
    kpiLeave: document.getElementById('kpiLeave'),
    kpiPayroll: document.getElementById('kpiPayroll'),
    kpiAvgSalary: document.getElementById('kpiAvgSalary'),
    deptBreakdownContainer: document.getElementById('deptBreakdownContainer'),

    // Controls
    searchInput: document.getElementById('searchInput'),
    clearSearchBtn: document.getElementById('clearSearchBtn'),
    departmentFilter: document.getElementById('departmentFilter'),
    statusFilter: document.getElementById('statusFilter'),
    exportBtn: document.getElementById('exportBtn'),
    refreshBtn: document.getElementById('refreshBtn'),
    resetFiltersBtn: document.getElementById('resetFiltersBtn'),

    // Table
    employeeTableBody: document.getElementById('employeeTableBody'),
    tableLoading: document.getElementById('tableLoading'),
    emptyState: document.getElementById('emptyState'),
    recordCountText: document.getElementById('recordCountText'),

    // Add / Edit Modal
    employeeModal: document.getElementById('employeeModal'),
    modalTitle: document.getElementById('modalTitle'),
    modalSubtitle: document.getElementById('modalSubtitle'),
    modalBadgeIcon: document.getElementById('modalBadgeIcon'),
    employeeForm: document.getElementById('employeeForm'),
    employeeId: document.getElementById('employeeId'),
    firstName: document.getElementById('firstName'),
    lastName: document.getElementById('lastName'),
    email: document.getElementById('email'),
    phone: document.getElementById('phone'),
    department: document.getElementById('department'),
    position: document.getElementById('position'),
    salary: document.getElementById('salary'),
    hireDate: document.getElementById('hireDate'),
    status: document.getElementById('status'),
    openAddModalBtn: document.getElementById('openAddModalBtn'),
    closeEmployeeModalBtn: document.getElementById('closeEmployeeModalBtn'),
    cancelFormBtn: document.getElementById('cancelFormBtn'),
    saveBtnText: document.getElementById('saveBtnText'),

    // View Modal
    viewModal: document.getElementById('viewModal'),
    viewModalBody: document.getElementById('viewModalBody'),
    closeViewModalBtn: document.getElementById('closeViewModalBtn'),
    closeViewBtn: document.getElementById('closeViewBtn'),
    editFromViewBtn: document.getElementById('editFromViewBtn'),

    // Delete Modal
    deleteModal: document.getElementById('deleteModal'),
    deleteEmployeeName: document.getElementById('deleteEmployeeName'),
    closeDeleteModalBtn: document.getElementById('closeDeleteModalBtn'),
    cancelDeleteBtn: document.getElementById('cancelDeleteBtn'),
    confirmDeleteBtn: document.getElementById('confirmDeleteBtn'),

    // Toast Container
    toastContainer: document.getElementById('toastContainer')
};

// ==========================================
// Initialization & Event Listeners
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    loadDashboardData();
});

function initEventListeners() {
    // Search input with debounce
    let debounceTimer;
    elements.searchInput.addEventListener('input', (e) => {
        const val = e.target.value.trim();
        elements.clearSearchBtn.style.display = val ? 'block' : 'none';
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            state.searchQuery = val;
            loadEmployees();
        }, 300);
    });

    elements.clearSearchBtn.addEventListener('click', () => {
        elements.searchInput.value = '';
        elements.clearSearchBtn.style.display = 'none';
        state.searchQuery = '';
        loadEmployees();
    });

    // Filters
    elements.departmentFilter.addEventListener('change', (e) => {
        state.selectedDepartment = e.target.value;
        loadEmployees();
    });

    elements.statusFilter.addEventListener('change', (e) => {
        state.selectedStatus = e.target.value;
        loadEmployees();
    });

    elements.refreshBtn.addEventListener('click', () => {
        elements.refreshBtn.classList.add('fa-spin');
        loadDashboardData().finally(() => {
            setTimeout(() => elements.refreshBtn.classList.remove('fa-spin'), 600);
        });
    });

    elements.resetFiltersBtn.addEventListener('click', resetFilters);
    elements.exportBtn.addEventListener('click', exportToCSV);

    // Modal controls
    elements.openAddModalBtn.addEventListener('click', openAddModal);
    elements.closeEmployeeModalBtn.addEventListener('click', closeEmployeeModal);
    elements.cancelFormBtn.addEventListener('click', closeEmployeeModal);
    elements.employeeForm.addEventListener('submit', handleFormSubmit);

    // View Modal controls
    elements.closeViewModalBtn.addEventListener('click', closeViewModal);
    elements.closeViewBtn.addEventListener('click', closeViewModal);
    elements.editFromViewBtn.addEventListener('click', () => {
        if (state.currentEmployee) {
            closeViewModal();
            openEditModal(state.currentEmployee);
        }
    });

    // Delete Modal controls
    elements.closeDeleteModalBtn.addEventListener('click', closeDeleteModal);
    elements.cancelDeleteBtn.addEventListener('click', closeDeleteModal);
    elements.confirmDeleteBtn.addEventListener('click', confirmDelete);

    // Close modal on click outside dialog
    [elements.employeeModal, elements.viewModal, elements.deleteModal].forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.add('hidden');
            }
        });
    });

    // Set default hire date to today in form
    elements.hireDate.value = new Date().toISOString().split('T')[0];
}

// ==========================================
// API Interaction & Data Loading
// ==========================================
async function loadDashboardData() {
    await Promise.all([
        loadStats(),
        loadEmployees()
    ]);
}

async function loadStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (!response.ok) throw new Error('Failed to fetch statistics');
        const stats = await response.json();
        state.stats = stats;
        renderStats(stats);
        updateApiStatus(true);
    } catch (error) {
        console.warn('Stats fetch issue:', error);
        updateApiStatus(false);
    }
}

async function loadEmployees() {
    showLoading(true);
    try {
        let url = new URL(API_BASE_URL, window.location.origin);
        if (state.searchQuery) {
            url.searchParams.set('search', state.searchQuery);
        }
        if (state.selectedDepartment !== 'ALL') {
            url.searchParams.set('department', state.selectedDepartment);
        }
        if (state.selectedStatus !== 'ALL') {
            url.searchParams.set('status', state.selectedStatus);
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP error ${response.status}`);
        const employees = await response.json();
        state.employees = employees;
        renderEmployees(employees);
        updateApiStatus(true);
    } catch (error) {
        console.error('Error fetching employees:', error);
        showToast('Error connecting to backend API. Please verify server is running.', 'error');
        updateApiStatus(false);
        renderEmployees([]);
    } finally {
        showLoading(false);
    }
}

// ==========================================
// Rendering Functions
// ==========================================
function renderStats(stats) {
    if (!stats) return;

    elements.kpiTotal.textContent = stats.totalEmployees || 0;
    elements.kpiActive.textContent = stats.activeEmployees || 0;
    
    const total = stats.totalEmployees || 0;
    const activePct = total > 0 ? Math.round((stats.activeEmployees / total) * 100) : 0;
    elements.kpiActivePercent.textContent = `${activePct}% of total workforce`;

    const nonActive = (stats.onLeaveEmployees || 0) + (stats.inactiveEmployees || 0);
    elements.kpiLeave.textContent = nonActive;

    const payroll = stats.totalPayroll || 0;
    elements.kpiPayroll.textContent = formatCurrency(payroll);

    const avgSalary = stats.averageSalary || 0;
    elements.kpiAvgSalary.textContent = `Avg: ${formatCurrency(avgSalary)}/yr`;

    // Render department breakdown chips
    elements.deptBreakdownContainer.innerHTML = '';
    const breakdown = stats.departmentBreakdown || {};

    const allPill = document.createElement('div');
    allPill.className = `dept-pill ${state.selectedDepartment === 'ALL' ? 'active' : ''}`;
    allPill.innerHTML = `<span>All</span> <span class="dept-badge-count">${total}</span>`;
    allPill.addEventListener('click', () => {
        elements.departmentFilter.value = 'ALL';
        state.selectedDepartment = 'ALL';
        loadEmployees();
        highlightActiveDeptPill('ALL');
    });
    elements.deptBreakdownContainer.appendChild(allPill);

    Object.entries(breakdown).forEach(([dept, count]) => {
        const pill = document.createElement('div');
        pill.className = `dept-pill ${state.selectedDepartment === dept ? 'active' : ''}`;
        pill.innerHTML = `<span>${escapeHtml(dept)}</span> <span class="dept-badge-count">${count}</span>`;
        pill.addEventListener('click', () => {
            elements.departmentFilter.value = dept;
            state.selectedDepartment = dept;
            loadEmployees();
            highlightActiveDeptPill(dept);
        });
        elements.deptBreakdownContainer.appendChild(pill);
    });
}

function highlightActiveDeptPill(selectedDept) {
    document.querySelectorAll('.dept-pill').forEach(pill => {
        const text = pill.querySelector('span').textContent;
        if (text === selectedDept || (selectedDept === 'ALL' && text === 'All')) {
            pill.classList.add('active');
        } else {
            pill.classList.remove('active');
        }
    });
}

function renderEmployees(employees) {
    elements.employeeTableBody.innerHTML = '';
    elements.recordCountText.textContent = `Showing ${employees.length} employee${employees.length === 1 ? '' : 's'}`;

    if (employees.length === 0) {
        elements.emptyState.classList.remove('hidden');
        return;
    }

    elements.emptyState.classList.add('hidden');

    employees.forEach(emp => {
        const tr = document.createElement('tr');
        const initials = getInitials(emp.firstName, emp.lastName);
        const statusClass = (emp.status || 'ACTIVE').toLowerCase();
        const statusLabel = formatStatus(emp.status);

        tr.innerHTML = `
            <td>
                <div class="user-cell">
                    <div class="user-avatar">${initials}</div>
                    <div class="user-info">
                        <span class="user-name">${escapeHtml(emp.firstName)} ${escapeHtml(emp.lastName)}</span>
                        <span class="user-email">${escapeHtml(emp.email)}</span>
                    </div>
                </div>
            </td>
            <td>
                <span class="badge badge-dept">${escapeHtml(emp.department)}</span>
            </td>
            <td>${escapeHtml(emp.position)}</td>
            <td class="salary-text">${formatCurrency(emp.salary)}</td>
            <td>${formatDate(emp.hireDate)}</td>
            <td>
                <span class="badge badge-${statusClass}">
                    <i class="fa-solid fa-circle" style="font-size: 0.45rem;"></i>
                    ${statusLabel}
                </span>
            </td>
            <td class="text-right">
                <div class="action-buttons">
                    <button class="btn-action view" title="View Details" data-id="${emp.id}">
                        <i class="fa-solid fa-eye"></i>
                    </button>
                    <button class="btn-action edit" title="Edit Employee" data-id="${emp.id}">
                        <i class="fa-solid fa-pencil"></i>
                    </button>
                    <button class="btn-action delete" title="Delete Employee" data-id="${emp.id}">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                </div>
            </td>
        `;

        // Attach action events
        tr.querySelector('.btn-action.view').addEventListener('click', () => openViewModal(emp));
        tr.querySelector('.btn-action.edit').addEventListener('click', () => openEditModal(emp));
        tr.querySelector('.btn-action.delete').addEventListener('click', () => openDeleteModal(emp));

        elements.employeeTableBody.appendChild(tr);
    });
}

function showLoading(isLoading) {
    if (isLoading) {
        elements.tableLoading.classList.remove('hidden');
        elements.emptyState.classList.add('hidden');
    } else {
        elements.tableLoading.classList.add('hidden');
    }
}

function updateApiStatus(isOnline) {
    if (isOnline) {
        elements.apiStatusBadge.innerHTML = `
            <span class="status-indicator online"></span>
            <span>Connected (Spring Boot API)</span>
        `;
    } else {
        elements.apiStatusBadge.innerHTML = `
            <span class="status-indicator offline"></span>
            <span>Backend Disconnected</span>
        `;
    }
}

// ==========================================
// Modal Operations (Add, Edit, View, Delete)
// ==========================================
function openAddModal() {
    clearFormErrors();
    elements.employeeForm.reset();
    elements.employeeId.value = '';
    elements.modalTitle.textContent = 'Add New Employee';
    elements.modalSubtitle.textContent = 'Enter employee credentials and job assignment details.';
    elements.modalBadgeIcon.innerHTML = '<i class="fa-solid fa-user-plus"></i>';
    elements.saveBtnText.textContent = 'Save Employee';
    elements.hireDate.value = new Date().toISOString().split('T')[0];
    elements.status.value = 'ACTIVE';
    elements.employeeModal.classList.remove('hidden');
    elements.firstName.focus();
}

function openEditModal(employee) {
    clearFormErrors();
    elements.employeeId.value = employee.id;
    elements.firstName.value = employee.firstName || '';
    elements.lastName.value = employee.lastName || '';
    elements.email.value = employee.email || '';
    elements.phone.value = employee.phone || '';
    elements.department.value = employee.department || '';
    elements.position.value = employee.position || '';
    elements.salary.value = employee.salary || '';
    elements.hireDate.value = employee.hireDate || '';
    elements.status.value = employee.status || 'ACTIVE';

    elements.modalTitle.textContent = 'Edit Employee Profile';
    elements.modalSubtitle.textContent = `Update records for ${employee.firstName} ${employee.lastName}.`;
    elements.modalBadgeIcon.innerHTML = '<i class="fa-solid fa-user-pen"></i>';
    elements.saveBtnText.textContent = 'Update Employee';
    elements.employeeModal.classList.remove('hidden');
}

function closeEmployeeModal() {
    elements.employeeModal.classList.add('hidden');
}

function openViewModal(employee) {
    state.currentEmployee = employee;
    const initials = getInitials(employee.firstName, employee.lastName);
    const statusClass = (employee.status || 'ACTIVE').toLowerCase();
    const statusLabel = formatStatus(employee.status);
    const tenure = calculateTenure(employee.hireDate);

    elements.viewModalBody.innerHTML = `
        <div class="profile-card">
            <div class="profile-top">
                <div class="profile-avatar-large">${initials}</div>
                <div>
                    <h3 style="font-size: 1.25rem; font-weight: 700;">${escapeHtml(employee.firstName)} ${escapeHtml(employee.lastName)}</h3>
                    <p style="color: var(--text-secondary); font-size: 0.875rem;">${escapeHtml(employee.position)} • <strong>${escapeHtml(employee.department)}</strong></p>
                    <div style="margin-top: 0.4rem;">
                        <span class="badge badge-${statusClass}">
                            <i class="fa-solid fa-circle" style="font-size: 0.45rem;"></i>
                            ${statusLabel}
                        </span>
                    </div>
                </div>
            </div>

            <div class="profile-details-grid">
                <div class="detail-item">
                    <span class="detail-label">Employee ID</span>
                    <span class="detail-value">#EMP-${String(employee.id).padStart(4, '0')}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Work Email</span>
                    <span class="detail-value">${escapeHtml(employee.email)}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Phone Number</span>
                    <span class="detail-value">${employee.phone ? escapeHtml(employee.phone) : '—'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Annual Salary</span>
                    <span class="detail-value">${formatCurrency(employee.salary)} / year</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Hire Date</span>
                    <span class="detail-value">${formatDate(employee.hireDate)}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Tenure</span>
                    <span class="detail-value">${tenure}</span>
                </div>
            </div>
        </div>
    `;

    elements.viewModal.classList.remove('hidden');
}

function closeViewModal() {
    elements.viewModal.classList.add('hidden');
    state.currentEmployee = null;
}

function openDeleteModal(employee) {
    state.currentEmployee = employee;
    elements.deleteEmployeeName.textContent = `${employee.firstName} ${employee.lastName} (ID: #${employee.id})`;
    elements.deleteModal.classList.remove('hidden');
}

function closeDeleteModal() {
    elements.deleteModal.classList.add('hidden');
    state.currentEmployee = null;
}

// ==========================================
// Form Submission & Validation
// ==========================================
async function handleFormSubmit(e) {
    e.preventDefault();
    if (state.isSubmitting) return;

    clearFormErrors();
    const id = elements.employeeId.value;
    const isEdit = Boolean(id);

    const payload = {
        firstName: elements.firstName.value.trim(),
        lastName: elements.lastName.value.trim(),
        email: elements.email.value.trim(),
        phone: elements.phone.value.trim() || null,
        department: elements.department.value,
        position: elements.position.value.trim(),
        salary: elements.salary.value ? parseFloat(elements.salary.value) : null,
        hireDate: elements.hireDate.value,
        status: elements.status.value
    };

    // Client-side quick check
    let hasClientError = false;
    if (!payload.firstName) { showFieldError('firstName', 'First name is required'); hasClientError = true; }
    if (!payload.lastName) { showFieldError('lastName', 'Last name is required'); hasClientError = true; }
    if (!payload.email) { showFieldError('email', 'Email is required'); hasClientError = true; }
    if (!payload.department) { showFieldError('department', 'Please select a department'); hasClientError = true; }
    if (!payload.position) { showFieldError('position', 'Position is required'); hasClientError = true; }
    if (!payload.salary || payload.salary <= 0) { showFieldError('salary', 'Please enter a valid positive salary'); hasClientError = true; }
    if (!payload.hireDate) { showFieldError('hireDate', 'Hire date is required'); hasClientError = true; }

    if (hasClientError) return;

    state.isSubmitting = true;
    const submitBtn = elements.employeeForm.querySelector('button[type="submit"]');
    const originalText = elements.saveBtnText.textContent;
    elements.saveBtnText.textContent = isEdit ? 'Updating...' : 'Saving...';
    submitBtn.disabled = true;

    try {
        const url = isEdit ? `${API_BASE_URL}/${id}` : API_BASE_URL;
        const method = isEdit ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok) {
            if (response.status === 400 && data.validationErrors) {
                // Display server-side bean validation errors
                Object.entries(data.validationErrors).forEach(([field, msg]) => {
                    showFieldError(field, msg);
                });
                throw new Error('Please fix the indicated validation errors.');
            } else if (response.status === 409) {
                showFieldError('email', data.message || 'Email already exists');
                throw new Error(data.message || 'Email conflict');
            } else {
                throw new Error(data.message || 'Operation failed');
            }
        }

        closeEmployeeModal();
        showToast(isEdit ? 'Employee updated successfully!' : 'Employee added successfully!', 'success');
        await loadDashboardData();
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        state.isSubmitting = false;
        elements.saveBtnText.textContent = originalText;
        submitBtn.disabled = false;
    }
}

async function confirmDelete() {
    if (!state.currentEmployee) return;
    const id = state.currentEmployee.id;
    const btn = elements.confirmDeleteBtn;
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Deleting...';

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || 'Failed to delete employee');
        }

        closeDeleteModal();
        showToast('Employee record deleted permanently', 'success');
        await loadDashboardData();
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-trash-can"></i> <span>Delete Employee</span>';
    }
}

// ==========================================
// CSV Export & Filter Reset
// ==========================================
function exportToCSV() {
    if (!state.employees || state.employees.length === 0) {
        showToast('No records available to export', 'error');
        return;
    }

    const headers = ['ID', 'First Name', 'Last Name', 'Email', 'Phone', 'Department', 'Position', 'Salary', 'Hire Date', 'Status'];
    const rows = state.employees.map(e => [
        e.id,
        `"${(e.firstName || '').replace(/"/g, '""')}"`,
        `"${(e.lastName || '').replace(/"/g, '""')}"`,
        `"${(e.email || '').replace(/"/g, '""')}"`,
        `"${(e.phone || '').replace(/"/g, '""')}"`,
        `"${(e.department || '').replace(/"/g, '""')}"`,
        `"${(e.position || '').replace(/"/g, '""')}"`,
        e.salary,
        e.hireDate,
        e.status
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + 
        [headers.join(','), ...rows.map(r => r.join(','))].join('\n');

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `employees_export_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showToast('Employee list exported to CSV', 'success');
}

function resetFilters() {
    elements.searchInput.value = '';
    elements.clearSearchBtn.style.display = 'none';
    elements.departmentFilter.value = 'ALL';
    elements.statusFilter.value = 'ALL';
    state.searchQuery = '';
    state.selectedDepartment = 'ALL';
    state.selectedStatus = 'ALL';
    highlightActiveDeptPill('ALL');
    loadEmployees();
}

// ==========================================
// Helper Utilities
// ==========================================
function showFieldError(fieldName, message) {
    const input = document.getElementById(fieldName);
    const errSpan = document.getElementById(`${fieldName}Error`);
    if (input) input.classList.add('invalid');
    if (errSpan) errSpan.textContent = message;
}

function clearFormErrors() {
    document.querySelectorAll('.field-error').forEach(el => el.textContent = '');
    document.querySelectorAll('.form-group input, .form-group select').forEach(el => el.classList.remove('invalid'));
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation';
    
    toast.innerHTML = `
        <div class="toast-icon"><i class="fa-solid ${icon}"></i></div>
        <div class="toast-content">${escapeHtml(message)}</div>
        <button class="toast-close" aria-label="Close">&times;</button>
    `;

    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.remove();
    });

    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 250);
    }, 4000);
}

function formatCurrency(amount) {
    if (amount === undefined || amount === null) return '$0';
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        maximumFractionDigits: 0
    }).format(amount);
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        const d = new Date(parts[0], parts[1] - 1, parts[2]);
        return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    }
    return dateStr;
}

function formatStatus(status) {
    if (!status) return 'Active';
    switch (status.toUpperCase()) {
        case 'ACTIVE': return 'Active';
        case 'ON_LEAVE': return 'On Leave';
        case 'INACTIVE': return 'Inactive';
        default: return status;
    }
}

function getInitials(firstName, lastName) {
    const f = (firstName || '').charAt(0).toUpperCase();
    const l = (lastName || '').charAt(0).toUpperCase();
    return (f + l) || 'EM';
}

function calculateTenure(hireDateStr) {
    if (!hireDateStr) return '—';
    const hire = new Date(hireDateStr);
    const now = new Date();
    const diffMonths = (now.getFullYear() - hire.getFullYear()) * 12 + (now.getMonth() - hire.getMonth());
    if (diffMonths < 1) return 'Less than a month';
    const years = Math.floor(diffMonths / 12);
    const months = diffMonths % 12;
    let res = [];
    if (years > 0) res.push(`${years} yr${years > 1 ? 's' : ''}`);
    if (months > 0) res.push(`${months} mo${months > 1 ? 's' : ''}`);
    return res.join(' ') || '< 1 month';
}

function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
