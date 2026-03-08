/* Central Navigation and Interaction Logic
   AI Research War-Room
*/

const Router = {
    // Navigation helper
    navigateTo(page) {
        if (!page) return;
        // Basic mapping for human-readable aliases if needed
        const routes = {
            'dashboard': 'dashboard.html',
            'projects': 'my_upload.html',
            'reports': 'reports.html',
            'report_view': 'report_view.html',
            'signin': 'signin.html',
            'profile': 'profile.html',
            'settings': 'settings_beach.html',
            'debate': 'debate.html'
        };
        const target = routes[page] || page;
        window.location.href = target;
    },

    // Dropdown Utilities
    toggleDropdown(menuId, buttonId) {
        const menu = document.getElementById(menuId);
        const button = document.getElementById(buttonId);
        if (!menu) return;

        const isOpen = menu.classList.contains('active');

        // Use standard active class for visibility
        if (isOpen) {
            menu.classList.remove('active');
            if (button) button.setAttribute('aria-expanded', 'false');
        } else {
            menu.classList.add('active');
            if (button) button.setAttribute('aria-expanded', 'true');
        }
    },

    closeDropdownOutside(menuId, buttonId, event) {
        const menu = document.getElementById(menuId);
        const button = document.getElementById(buttonId);
        if (!menu || !menu.classList.contains('active')) return;

        if (!menu.contains(event.target) && !button.contains(event.target)) {
            menu.classList.remove('active');
            if (button) button.setAttribute('aria-expanded', 'false');
        }
    }
};

// Global Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    // Handle Navbar items with data-route
    document.querySelectorAll('[data-route]').forEach(el => {
        el.addEventListener('click', (e) => {
            e.preventDefault();
            Router.navigateTo(el.dataset.route);
        });
    });

    // Sign In Button routing
    const signInBtn = document.getElementById('signInBtn');
    if (signInBtn) {
        signInBtn.addEventListener('click', () => Router.navigateTo('signin.html'));
    }

    // User Avatar Dropdown
    const userAvatarBtn = document.getElementById('userAvatarBtn');
    const userDropdown = document.getElementById('userDropdown');

    if (userAvatarBtn && userDropdown) {
        userAvatarBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            Router.toggleDropdown('userDropdown', 'userAvatarBtn');
        });

        // Close when clicking outside
        document.addEventListener('click', (e) => {
            Router.closeDropdownOutside('userDropdown', 'userAvatarBtn', e);
        });

        // Dropdown Item Routing
        userDropdown.querySelectorAll('.dropdown-item').forEach(item => {
            item.addEventListener('click', (e) => {
                const path = item.dataset.path;
                if (path) Router.navigateTo(path);
            });
        });
    }

    // Modal/Notifications (Placeholder/Basic)
    const notificationsBtn = document.getElementById('notificationsBtn');
    if (notificationsBtn) {
        notificationsBtn.addEventListener('click', () => {
            alert('Notifications functionality coming soon.');
        });
    }

    // Sign-in Form logic
    const signinForm = document.getElementById('signin-form');
    if (signinForm) {
        signinForm.addEventListener('submit', (e) => {
            e.preventDefault();
            Router.navigateTo('dashboard.html');
        });
    }

    // Report Page Logic
    const copySummaryBtn = document.getElementById('copySummaryBtn');
    if (copySummaryBtn) {
        copySummaryBtn.addEventListener('click', () => {
            const summaryText = document.querySelector('.report-summary-content')?.innerText || "Summary text here...";
            navigator.clipboard.writeText(summaryText).then(() => {
                const originalText = copySummaryBtn.innerHTML;
                copySummaryBtn.innerHTML = '<span class="material-symbols-outlined">check</span> Copied!';
                setTimeout(() => copySummaryBtn.innerHTML = originalText, 2000);
            });
        });
    }

    const downloadPdfBtn = document.getElementById('downloadPdfBtn');
    if (downloadPdfBtn) {
        downloadPdfBtn.addEventListener('click', () => {
            window.print();
        });
    }
});
