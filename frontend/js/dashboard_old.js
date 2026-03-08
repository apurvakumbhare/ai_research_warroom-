/* dashboard.js */
document.addEventListener('DOMContentLoaded', () => {
    console.log('Dashboard loaded');

    // Simulated chart animation
    const bars = document.querySelectorAll('.chart-bar');
    bars.forEach((bar, index) => {
        const targetHeight = bar.style.height;
        bar.style.height = '0%';
        setTimeout(() => {
            bar.style.height = targetHeight;
        }, index * 100);
    });

    // Task 1: Sign In Button Routing
    const signInBtn = document.getElementById('signInBtn');
    if (signInBtn) {
        signInBtn.addEventListener('click', () => {
            window.location.href = 'signin.html';
        });
    }

    // Task 2 & 4: User Profile Dropdown Interaction
    const avatarBtn = document.getElementById('userAvatarBtn');
    const dropdown = document.getElementById('userDropdown');

    if (avatarBtn && dropdown) {
        const toggleDropdown = () => {
            const isExpanded = avatarBtn.getAttribute('aria-expanded') === 'true';
            avatarBtn.setAttribute('aria-expanded', !isExpanded);
            dropdown.classList.toggle('active');
        };

        const closeDropdown = () => {
            avatarBtn.setAttribute('aria-expanded', 'false');
            dropdown.classList.remove('active');
        };

        avatarBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            toggleDropdown();
        });

        // Close when clicking outside
        document.addEventListener('click', (e) => {
            if (!dropdown.contains(e.target) && e.target !== avatarBtn) {
                closeDropdown();
            }
        });

        // Task 3: Dropdown Routing
        const menuItems = dropdown.querySelectorAll('.dropdown-item');
        menuItems.forEach(item => {
            item.addEventListener('click', () => {
                const path = item.getAttribute('data-path');
                if (path) {
                    window.location.href = path;
                }
            });
        });

        // Task 5: Accessibility - Keyboard support
        avatarBtn.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                toggleDropdown();
            } else if (e.key === 'Escape') {
                closeDropdown();
            }
        });

        dropdown.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closeDropdown();
                avatarBtn.focus();
            }
        });
    }
});
