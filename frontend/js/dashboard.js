/* dashboard.js - Simulated interactions for the Intelligence Overview */
document.addEventListener('DOMContentLoaded', () => {
    // Simulated chart animation
    const bars = document.querySelectorAll('.chart-bar');
    bars.forEach((bar, index) => {
        const targetHeight = bar.style.height;
        bar.style.height = '0%';
        setTimeout(() => {
            bar.style.height = targetHeight;
        }, index * 100);
    });

    // Recent project card hover effects or clicks could go here
    // Navigation is handled by navigation.js

    // View All Projects Toggle
    const viewAllBtn = document.getElementById('viewAllProjectsBtn');
    const extraProjects = document.querySelectorAll('.project-extra');

    if (viewAllBtn && extraProjects.length > 0) {
        viewAllBtn.addEventListener('click', () => {
            const isExpanded = viewAllBtn.getAttribute('aria-expanded') === 'true';

            if (isExpanded) {
                // Hide extras
                extraProjects.forEach(el => el.style.display = 'none');
                viewAllBtn.setAttribute('aria-expanded', 'false');
                viewAllBtn.textContent = 'View All Projects';
            } else {
                // Show extras
                extraProjects.forEach(el => el.style.display = 'flex');
                viewAllBtn.setAttribute('aria-expanded', 'true');
                viewAllBtn.textContent = 'Show Less';
            }
        });
    }
});
