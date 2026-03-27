/**
 * profile.js
 * Fetches user profile from the backend and populates the profile.html page.
 */

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('authToken');
    if (!token) {
        console.warn('No auth token found, redirecting to signin');
        window.location.href = 'signin.html';
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/users/profile', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                localStorage.clear();
                window.location.href = 'signin.html';
                return;
            }
            throw new Error('Failed to fetch profile');
        }

        const user = await response.json();

        // Populate fields
        updateElement('user-full-name-heading', user.fullName);
        updateElement('user-full-name-display', user.fullName);
        updateElement('user-role-display', user.role);
        updateElement('user-email-display', user.email);
        updateElement('user-dob-display', user.dob);
        updateElement('user-joined-display', user.joinedDate);

        // Update sidebar and other locations
        const sidebarRole = document.querySelector('.profile-avatar-card p');
        if (sidebarRole) sidebarRole.textContent = user.role.toUpperCase();

    } catch (error) {
        console.error('Profile load error:', error);
    }
});

function updateElement(id, value) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = value || 'N/A';
    }
}
