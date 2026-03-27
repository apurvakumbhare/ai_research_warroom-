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

    async function loadProjects() {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) return;

            const res = await fetch('http://localhost:8080/api/chat/recent', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!res.ok) return;

            let projects = await res.json();
            const listContainer = document.getElementById('projects-list');
            if (!listContainer || projects.length === 0) return;

            listContainer.innerHTML = '';
            
            // Since /api/chat/recent returns them filtered and sorted, we don't need reverse()
            // projects.reverse();

            projects.forEach((proj, index) => {
                const isExtra = index >= 2;
                const div = document.createElement('div');
                div.className = `project-row flex items-center gap-4 p-3 hover:bg-slate-50 cursor-pointer ${isExtra ? 'project-extra' : ''}`;
                div.style.borderRadius = '0.75rem';
                if (isExtra) div.style.display = 'none';
                div.tabIndex = 0;
                div.setAttribute('role', 'button');
                
                // Allow row click to navigate
                div.onclick = (e) => {
                    // Prevent navigation if they click the PDF button
                    if (e.target.closest('.pdf-dl-btn')) return;
                    window.location.href = `debate.html?chatSessionId=${proj.chatSessionId}`;
                };

                const isCompleted = proj.isEnded || proj.status === 'COMPLETED';
                const pdfButtonHTML = isCompleted ? `
                    <button class="pdf-dl-btn" title="Download Summary PDF" onclick="event.stopPropagation(); window.dashboardApp.generateProjectPDF('${proj.chatSessionId}', '${(proj.title || '').replace(/'/g, "\\'")}')" style="background:var(--slate-100); border:none; border-radius:50%; width:32px; height:32px; display:flex; align-items:center; justify-content:center; cursor:pointer; color:var(--primary); transition:background 0.2s; margin-right:8px;">
                        <span class="material-symbols-outlined" style="font-size:18px;">download</span>
                    </button>
                ` : '';

                div.innerHTML = `
                    <div class="message-avatar" style="background: var(--primary-light); color: var(--primary-dark);">
                        <span class="material-symbols-outlined">folder</span>
                    </div>
                    <div class="flex-1">
                        <p style="font-size: 0.875rem; font-weight: 700;">${proj.title || 'Untitled Project'}</p>
                        <p style="font-size: 0.75rem; color: var(--slate-400);">${proj.status || 'Active'}</p>
                    </div>
                    ${pdfButtonHTML}
                    <span class="material-symbols-outlined" style="color: var(--slate-300);">chevron_right</span>
                `;
                listContainer.appendChild(div);
            });

            if (viewAllBtn && extraProjects.length > 0) {
                // Re-bind View All (requires fresh querySelectorAll because we overwrote the DOM)
                const currentExtras = document.querySelectorAll('.project-extra');
                if (currentExtras.length === 0) {
                    viewAllBtn.style.display = 'none';
                } else {
                    viewAllBtn.onclick = () => {
                        const isExpanded = viewAllBtn.getAttribute('aria-expanded') === 'true';
                        if (isExpanded) {
                            currentExtras.forEach(el => el.style.display = 'none');
                            viewAllBtn.setAttribute('aria-expanded', 'false');
                            viewAllBtn.textContent = 'View All Projects';
                        } else {
                            currentExtras.forEach(el => el.style.display = 'flex');
                            viewAllBtn.setAttribute('aria-expanded', 'true');
                            viewAllBtn.textContent = 'Show Less';
                        }
                    };
                }
            }
        } catch (err) {
            console.error('Failed to load projects:', err);
        }
    }

    loadProjects();
});

// Export a global handler so inline onclick="..." works
window.dashboardApp = {
    generateProjectPDF: async function(projectId, projectName) {
        if (!projectId) return;
        
        // Let the user know we're generating (basic indication)
        const toast = document.createElement('div');
        toast.textContent = 'Fetching transcript for PDF...';
        toast.style.cssText = 'position:fixed;bottom:20px;right:20px;background:#1e293b;color:white;padding:12px 24px;border-radius:8px;font-weight:600;z-index:9999;box-shadow:0 4px 12px rgba(0,0,0,0.15);';
        document.body.appendChild(toast);

        try {
            const token = localStorage.getItem('authToken');
            const res = await fetch(`http://localhost:8080/api/chat/${projectId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!res.ok) throw new Error('API return non-ok status');
            
            const data = await res.json();
            const messages = data.messages || [];
            
            if (messages.length === 0) {
                toast.textContent = 'No messages available to summarize.';
                setTimeout(() => toast.remove(), 3000);
                return;
            }

            toast.textContent = 'Generating PDF...';

            const { jsPDF } = window.jspdf;
            const doc = new jsPDF({ unit: 'pt', format: 'a4' });

            const pageW   = doc.internal.pageSize.getWidth();
            const margin  = 48;
            const maxW    = pageW - margin * 2;
            let   y       = margin;

            // ── Header
            doc.setFillColor(66, 133, 244);
            doc.rect(0, 0, pageW, 72, 'F');
            doc.setTextColor(255, 255, 255);
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(18);
            doc.text('AI Research War-Room', margin, 30);
            doc.setFontSize(11);
            doc.setFont('helvetica', 'normal');
            doc.text('Debate Summary', margin, 48);
            const now = new Date();
            doc.text(`Generated: ${now.toLocaleString()}`, margin, 64);
            y = 96;

            // ── Project info
            doc.setTextColor(30, 41, 59);
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(13);
            doc.text(`Project: ${projectName}`, margin, y);
            y += 24;

            // divider
            doc.setDrawColor(203, 213, 225);
            doc.line(margin, y, pageW - margin, y);
            y += 20;

            // ── Messages
            doc.setFont('helvetica', 'normal');
            doc.setTextColor(30, 41, 59);

            messages.forEach((msg) => {
                if (y > doc.internal.pageSize.getHeight() - 72) {
                    doc.addPage();
                    y = margin;
                }

                // Sender line
                doc.setFont('helvetica', 'bold');
                doc.setFontSize(9);
                doc.setTextColor(100, 116, 139);
                doc.text(`[${msg.time || ''}] ${msg.sender || 'Unknown'}`, margin, y);
                y += 14;

                // Content
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(10);
                doc.setTextColor(30, 41, 59);
                const contentLines = doc.splitTextToSize(msg.content || '', maxW);
                contentLines.forEach(line => {
                    if (y > doc.internal.pageSize.getHeight() - 60) {
                        doc.addPage();
                        y = margin;
                    }
                    doc.text(line, margin, y);
                    y += 14;
                });
                y += 8; // gap
            });

            // ── Footer
            const totalPages = doc.internal.getNumberOfPages();
            for (let i = 1; i <= totalPages; i++) {
                doc.setPage(i);
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(8);
                doc.setTextColor(148, 163, 184);
                doc.text(`AI Research War-Room · Page ${i} of ${totalPages}`, margin, doc.internal.pageSize.getHeight() - 24);
            }

            doc.save(`debate-summary-${projectName.replace(/\s+/g,'-').toLowerCase()}.pdf`);
            
            toast.textContent = '✅ PDF downloaded successfully.';
            toast.style.background = '#10b981';
            setTimeout(() => toast.remove(), 3000);
            
        } catch (err) {
            console.error('Failed to generate PDF:', err);
            toast.textContent = '❌ Failed to fetch transcript.';
            toast.style.background = '#dc2626';
            setTimeout(() => toast.remove(), 3000);
        }
    }
};
