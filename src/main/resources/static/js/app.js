document.addEventListener('DOMContentLoaded', function () {
    showToasts();
    enableAutoSubmit();
    setUpDeleteModal();
    setUpFormValidation();
});

function showToasts() {
    document.querySelectorAll('.toast').forEach(function (toast) {
        bootstrap.Toast.getOrCreateInstance(toast).show();
    });
}

function enableAutoSubmit() {
    document.querySelectorAll('[data-auto-submit]').forEach(function (select) {
        select.addEventListener('change', function () {
            select.form.requestSubmit();
        });
    });
}

function setUpDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (!modal) {
        return;
    }
    const form = document.getElementById('deleteForm');
    modal.addEventListener('show.bs.modal', function (event) {
        const trigger = event.relatedTarget;
        form.action = trigger.dataset.deleteUrl;
        document.getElementById('deleteEmployeeName').textContent = trigger.dataset.employeeName;
    });
    form.addEventListener('submit', function () {
        showLoading(form);
    });
}

function setUpFormValidation() {
    document.querySelectorAll('form.needs-validation').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                form.classList.add('was-validated');
                const firstInvalid = form.querySelector(':invalid');
                if (firstInvalid) {
                    firstInvalid.focus();
                }
                return;
            }
            showLoading(form);
        });

        form.querySelectorAll('.is-invalid').forEach(function (field) {
            field.addEventListener('input', function () {
                field.classList.remove('is-invalid');
            }, { once: true });
        });

        const firstServerError = form.querySelector('.is-invalid');
        if (firstServerError) {
            firstServerError.focus();
        }
    });
}

function showLoading(form) {
    const button = form.querySelector('button[type="submit"][data-loading-text]');
    if (!button) {
        return;
    }
    const spinner = document.createElement('span');
    spinner.className = 'spinner-border spinner-border-sm me-2';
    spinner.setAttribute('aria-hidden', 'true');

    button.disabled = true;
    button.replaceChildren(spinner, document.createTextNode(button.dataset.loadingText));
}
