/* main.js - Global UI Logic */

document.addEventListener('DOMContentLoaded', () => {
    console.log('AI Research War-Room initialized');

    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    // Sidebar toggle for mobile (if implemented)
    const sidebarToggle = document.getElementById('sidebar-toggle');
    const sidebar = document.querySelector('.dashboard-sidebar');
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }
});
