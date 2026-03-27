/* debate.js - Multi-agent debate with Pause/Resume, PDF Export, Throttle, Smart Scroll */

document.addEventListener('DOMContentLoaded', async () => {
    console.log('DEBATE JS VERSION 10 - chatSessionId logic active');
    console.log("DEBUG_MARKER_DISK_SYNC_123");
    // ── DOM refs ─────────────────────────────────────────────────────────────
    const timeline         = document.getElementById('timeline-container');
    const timelineScroll   = document.getElementById('debate-timeline');
    const pauseBtn         = document.getElementById('pause-btn');
    const pauseIcon        = document.getElementById('pause-icon');
    const pauseLabel       = document.getElementById('pause-label');
    const pauseBanner      = document.getElementById('pause-banner');
    const resumeInlineBtn  = document.getElementById('resume-inline-btn');
    const endChatBtn       = document.getElementById('end-chat-btn');
    const jumpBanner       = document.getElementById('jump-banner');
    const endedOverlay     = document.getElementById('ended-overlay');
    const dismissOverlayBtn= document.getElementById('dismiss-overlay-btn');
    const redownloadBtn    = document.getElementById('redownload-btn');
    const injectInput      = document.getElementById('inject-input');
    const injectBtn        = document.getElementById('inject-btn');
    const projectIntelList = document.getElementById('project-intel-list');
    const toastEl          = document.getElementById('toast');

    // File Viewer Modal refs
    const fileViewerOverlay = document.getElementById('file-viewer-overlay');
    const fileViewerTitle   = document.getElementById('file-viewer-title');
    const fileViewerContent = document.getElementById('file-viewer-content');
    const fileViewerCloseBtn= document.getElementById('file-viewer-close-btn');

    // Throttle controls
    const throttleEnabled  = document.getElementById('throttle-enabled');
    const delaySlider      = document.getElementById('delay-slider');
    const delayVal         = document.getElementById('delay-val');
    const maxcharsSlider   = document.getElementById('maxchars-slider');
    const maxcharsVal      = document.getElementById('maxchars-val');

    // ── State ─────────────────────────────────────────────────────────────────
    let messages           = [];
    let isPaused           = false;
    let isEnded            = false;
    let pollIntervalId     = null;
    let messageQueue       = [];
    let currentProjectId   = null; // To be populated from session metadata
    let isStreaming        = false;
    let isCompletedGlobal  = false;
    let currentStreamInterval = null;  // track active char-stream so we can pause it
    let userScrolledUp     = false;
    let unreadCount        = 0;
    let typingIndicator    = null;
    let lastPdfBlob        = null;     // keep for re-download
    let projectName        = 'Debate';

    // ── Throttle config ───────────────────────────────────────────────────────
    const THROTTLE = {
        enabled : true,
        delayMs : 2000,   // ms between messages
        maxChars: 800,    // max chars per AI message (0 = unlimited)
    };

    // ── Session ID ────────────────────────────────────────────────────────────
    const urlParams = new URLSearchParams(window.location.search);
    const chatSessionId = urlParams.get('chatSessionId') || urlParams.get('projectId') || localStorage.getItem('currentProjectId');

    if (!chatSessionId) {
        console.warn('No chatSessionId found, debate cannot start.');
        return;
    }

    const AGENT_CONFIG = {
        'STRATEGIST': { label: 'Strategist', icon: 'search_insights', color: '#4285F4', align: 'left'  },
        'CRITIC':     { label: 'Critic',     icon: 'gavel',           color: '#D97706', align: 'right' },
        'OPTIMIZER':  { label: 'Optimizer',  icon: 'lightbulb',       color: '#10A37F', align: 'left'  },
        'ARCHITECT':  { label: 'Architect',  icon: 'engineering',     color: '#7C3AED', align: 'right' },
        'SYNTHESIZER':{ label: 'Synthesizer',icon: 'summarize',       color: '#059669', align: 'left'  },
        'Coordinator':{ label: 'Coordinator',icon: 'support_agent',   color: '#0EA5E9', align: 'left'  },
    };
    const TURN_ORDER = ['STRATEGIST', 'CRITIC', 'OPTIMIZER', 'ARCHITECT'];

    // ─────────────────────────────────────────────────────────────────────────
    //  PROJECT INTEL DATA
    // ─────────────────────────────────────────────────────────────────────────
    async function fetchIntelDocuments() {
        if (!currentProjectId) {
            console.log('Postponing Intel fetch until projectId is available');
            return;
        }
        try {
            const token = localStorage.getItem('authToken');
            const resp = await fetch(`http://localhost:8080/api/projects/${currentProjectId}/uploads`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!resp.ok) throw new Error('API return non-ok status');
            
            const documents = await resp.json();
            projectIntelList.innerHTML = '';
            
            if (documents.length === 0) {
                projectIntelList.innerHTML = `<p style="font-size: 0.75rem; color: var(--slate-400); padding: 1rem; text-align: center;">No context files uploaded yet.</p>`;
                return;
            }

            documents.forEach(doc => {
                const el = document.createElement('div');
                el.className = 'p-3 card mb-4 hover:shadow-md transition-shadow cursor-pointer';
                el.style.borderRadius = '0.75rem';
                
                const badgeText = doc.fileName && doc.fileName.toLowerCase().endsWith('.pdf') ? 'PDF' : 'DOCUMENT';
                const badgeClass = badgeText === 'PDF' ? 'badge badge-blue' : 'badge';
                const badgeStyle = badgeText === 'PDF' ? '' : 'style="background: var(--sand-200); color: var(--driftwood); font-size: 10px;"';

                // We expect doc.uploadedAt, doc.fileName, doc.id
                el.innerHTML = `
                    <div class="flex justify-between mb-1">
                        <span class="${badgeClass}" ${badgeStyle} style="font-size: 10px;">${badgeText}</span>
                        <span style="font-size: 10px; color: var(--slate-400);">${doc.uploadedAt ? (String(doc.uploadedAt).split(' ')[0] || '') : ''}</span>
                    </div>
                    <h4 style="font-size: 0.875rem; font-weight: 700;">${doc.fileName || 'Untitled File'}</h4>
                    <p style="font-size: 0.75rem; color: var(--slate-500); display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-top: 4px;">
                        Click to view file content.
                    </p>
                `;
                
                // Add click handler to fetch full text and show modal
                el.addEventListener('click', () => openFileViewer(doc.id, doc.fileName));
                
                projectIntelList.appendChild(el);
            });
        } catch (err) {
            console.warn('Could not load project intel:', err);
            projectIntelList.innerHTML = `<p style="font-size: 0.75rem; color: #dc2626; padding: 1rem; text-align: center;">Failed to load context files: ${err.message}</p>`;
        }
    }
    
    async function openFileViewer(docId, fileName) {
        if (!currentProjectId) {
            console.warn('Cannot open file viewer: projectId not yet loaded');
            return;
        }
        
        const overlay = document.getElementById('file-viewer-overlay');
        const content = document.getElementById('file-viewer-content');
        const title   = document.getElementById('file-viewer-title');

        title.textContent = fileName;
        content.textContent = 'Loading execution context...';
        overlay.classList.add('visible'); // Changed from 'active' to 'visible' to match original

        try {
            const token = localStorage.getItem('authToken');
            
            // We fetch the files list to find the extracted text since the endpoint returns the list
            const docsRes = await fetch(`http://localhost:8080/api/chat/${chatSessionId}/files`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!docsRes.ok) throw new Error('File fetch failed');
            
            // Currently /files strips extractedText for performance, so we have to fallback to project endpoint or let it show limited context
            // To be robust without refactoring the single-document endpoints:
            const singleRes = await fetch(`http://localhost:8080/api/projects/${currentProjectId}/uploads/${docId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            const data = await singleRes.json();
            fileViewerContent.textContent = data.extractedText || 'No text content available for this file.';
        } catch (err) {
            console.error('Failed to load full document context:', err);
            fileViewerContent.innerHTML = `<span style="color:#dc2626;">Error: Could not retrieve file contents: ${err.message}</span>`;
        }
    }

    if (fileViewerCloseBtn) {
        fileViewerCloseBtn.addEventListener('click', () => {
            fileViewerOverlay.classList.remove('visible');
            fileViewerContent.textContent = '';
        });
    }

    fetchIntelDocuments();

    // ── Agent config ──────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILITY: Toast
    // ─────────────────────────────────────────────────────────────────────────
    function showToast(msg, durationMs = 3000) {
        toastEl.textContent = msg;
        toastEl.classList.add('show');
        setTimeout(() => toastEl.classList.remove('show'), durationMs);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SCROLL MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────
    function isAtBottom() {
        const el = timelineScroll;
        return el.scrollHeight - el.scrollTop - el.clientHeight < 120;
    }

    timelineScroll.addEventListener('scroll', () => {
        if (isAtBottom()) {
            userScrolledUp = false;
            unreadCount    = 0;
            hideJumpBanner();
        } else {
            userScrolledUp = true;
        }
    }, { passive: true });

    function scrollToBottom(force = false) {
        if (!userScrolledUp || force) {
            timelineScroll.scrollTo({ top: timelineScroll.scrollHeight, behavior: 'smooth' });
        }
    }

    function showJumpBanner() {
        jumpBanner.classList.add('visible');
        unreadCount++;
        jumpBanner.textContent = '';
        const icon = document.createElement('span');
        icon.className = 'material-symbols-outlined';
        icon.style.fontSize = '14px';
        icon.textContent = 'arrow_downward';
        jumpBanner.appendChild(icon);
        jumpBanner.append(` ${unreadCount} new message${unreadCount !== 1 ? 's' : ''} — Jump to latest`);
    }

    function hideJumpBanner() {
        jumpBanner.classList.remove('visible');
        unreadCount = 0;
    }

    jumpBanner.addEventListener('click', () => {
        userScrolledUp = false;
        scrollToBottom(true);
        hideJumpBanner();
    });

    // ─────────────────────────────────────────────────────────────────────────
    //  THROTTLE CONTROLS wiring
    // ─────────────────────────────────────────────────────────────────────────
    throttleEnabled.addEventListener('change', () => {
        THROTTLE.enabled = throttleEnabled.checked;
    });

    delaySlider.addEventListener('input', () => {
        THROTTLE.delayMs = parseFloat(delaySlider.value) * 1000;
        delayVal.textContent = `${delaySlider.value}s`;
    });

    maxcharsSlider.addEventListener('input', () => {
        THROTTLE.maxChars = parseInt(maxcharsSlider.value, 10);
        maxcharsVal.textContent = maxcharsSlider.value;
    });

    // ─────────────────────────────────────────────────────────────────────────
    //  PAUSE / RESUME
    // ─────────────────────────────────────────────────────────────────────────
    function applyPause() {
        isPaused = true;
        pauseBtn.setAttribute('aria-pressed', 'true');
        pauseBtn.setAttribute('aria-label', 'Resume debate');
        pauseIcon.textContent  = 'play_arrow';
        pauseLabel.textContent = 'RESUME';
        pauseBanner.classList.add('visible');

        // Freeze any active char-stream
        if (currentStreamInterval) {
            clearInterval(currentStreamInterval);
            currentStreamInterval = null;
            // Mark streaming as not in progress so queue can restart on resume
            isStreaming = false;
        }
    }

    function applyResume() {
        isPaused = false;
        pauseBtn.setAttribute('aria-pressed', 'false');
        pauseBtn.setAttribute('aria-label', 'Pause debate');
        pauseIcon.textContent  = 'pause';
        pauseLabel.textContent = 'PAUSE';
        pauseBanner.classList.remove('visible');

        // Drain the queue
        processMessageQueue();
    }

    pauseBtn.addEventListener('click', () => {
        if (isEnded) return;
        isPaused ? applyResume() : applyPause();
    });

    resumeInlineBtn.addEventListener('click', () => {
        if (!isPaused) return;
        applyResume();
    });

    // ─────────────────────────────────────────────────────────────────────────
    //  END CHAT + PDF EXPORT
    // ─────────────────────────────────────────────────────────────────────────
    async function generateAndDownloadPDF() {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF({ unit: 'pt', format: 'a4' });

        const pageW   = doc.internal.pageSize.getWidth();
        const margin  = 48;
        const maxW    = pageW - margin * 2;
        let   y       = margin;

        // ── Header ────────────────────────────────────────────────────────
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

        // ── Project info ──────────────────────────────────────────────────
        doc.setTextColor(30, 41, 59);
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(13);
        doc.text(`Project: ${projectName}`, margin, y);
        y += 18;

        const hypothesisEl = document.getElementById('project-hypothesis-text');
        if (hypothesisEl && hypothesisEl.textContent.trim()) {
            doc.setFont('helvetica', 'italic');
            doc.setFontSize(10);
            doc.setTextColor(100, 116, 139);
            const hLines = doc.splitTextToSize(hypothesisEl.textContent.trim(), maxW);
            doc.text(hLines, margin, y);
            y += hLines.length * 14 + 8;
        }

        // divider
        doc.setDrawColor(203, 213, 225);
        doc.line(margin, y, pageW - margin, y);
        y += 20;

        // ── Messages ──────────────────────────────────────────────────────
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
            y += 8; // gap between messages
        });

        // ── Footer on last page ───────────────────────────────────────────
        const totalPages = doc.internal.getNumberOfPages();
        for (let i = 1; i <= totalPages; i++) {
            doc.setPage(i);
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(8);
            doc.setTextColor(148, 163, 184);
            doc.text(`AI Research War-Room · Page ${i} of ${totalPages}`, margin, doc.internal.pageSize.getHeight() - 24);
        }

        // Save and return blob for re-download
        const filename = `debate-summary-${chatSessionId}.pdf`;
        doc.save(filename);
        lastPdfBlob = doc.output('blob');
        return filename;
    }

    endChatBtn.addEventListener('click', async () => {
        if (isEnded) return;
        if (!confirm('End this debate? A PDF summary will be generated and downloaded.')) return;

        isEnded = true;
        isPaused = true; // stop queue processing
        clearInterval(pollIntervalId);
        if (currentStreamInterval) {
            clearInterval(currentStreamInterval);
            currentStreamInterval = null;
        }

        endChatBtn.disabled = true;
        endChatBtn.innerHTML = '<span class="material-symbols-outlined" style="font-size:16px;">hourglass_top</span><span>GENERATING...</span>';

        // Disable input
        if (injectInput) injectInput.disabled = true;
        if (injectBtn) injectBtn.disabled = true;

        try {
            const token = localStorage.getItem('authToken');
            await fetch(`http://localhost:8080/api/chat/${chatSessionId}/end`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });
        } catch (err) {
            console.warn('Error calling debate end API:', err);
        }

        // Wait a beat to ensure any last poll messages are collected
        await new Promise(r => setTimeout(r, 500));

        try {
            const filename = await generateAndDownloadPDF();
            showToast(`✅ PDF downloaded: ${filename}`);
        } catch (err) {
            console.error('PDF generation failed:', err);
            showToast('⚠️ PDF generation failed — see console.');
        }

        // Show ended overlay
        endedOverlay.classList.add('visible');
        pauseBanner.classList.remove('visible');
        hideJumpBanner();
        if (typingIndicator) typingIndicator.remove();
        
        // Trap Focus
        redownloadBtn.focus();
    });

    dismissOverlayBtn.addEventListener('click', () => {
        endedOverlay.classList.remove('visible');
    });

    // Also close on Escape key if overlay is visible
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (endedOverlay.classList.contains('visible')) {
                endedOverlay.classList.remove('visible');
            }
            if (fileViewerOverlay && fileViewerOverlay.classList.contains('visible')) {
                fileViewerOverlay.classList.remove('visible');
            }
        }
    });

    redownloadBtn.addEventListener('click', async () => {
        try {
            await generateAndDownloadPDF();
            showToast('✅ PDF re-downloaded');
        } catch (err) {
            showToast('⚠️ Re-download failed');
        }
    });

    // ─────────────────────────────────────────────────────────────────────────
    //  MESSAGE STREAMING
    // ─────────────────────────────────────────────────────────────────────────
    function streamMessage(msg, callback) {
        const isRight = msg.align === 'right';

        // Apply max-chars truncation for AI agents (not user/system)
        let content = msg.content || '';
        const isAI = !(msg.sender || '').includes('User') && !(msg.sender || '').includes('Coordinator');
        if (isAI && THROTTLE.enabled && THROTTLE.maxChars > 0 && content.length > THROTTLE.maxChars) {
            content = content.slice(0, THROTTLE.maxChars) + '…';
        }

        const bgColor  = msg.color || 'var(--primary)';
        const modelBadge = msg.modelName
            ? `<span style="font-size: 9px; background: ${bgColor}22; color: ${bgColor}; padding: 1px 6px; border-radius: 4px; font-weight: 600;">${msg.modelName}</span>`
            : '';

        const card = document.createElement('div');
        card.className = `flex gap-4 max-w-2xl ${isRight ? 'ml-auto flex-row-reverse text-right' : ''}`;
        card.style.cssText = 'opacity:0; transform:translateY(20px); transition:all 0.4s ease;';

        const msgId = `msg-${Date.now()}-${Math.floor(Math.random() * 9999)}`;
        card.innerHTML = `
            <div class="message-avatar" style="background:${bgColor};width:40px;height:40px;flex-shrink:0;display:flex;align-items:center;justify-content:center;border-radius:50%;color:white;">
                <span class="material-symbols-outlined">${msg.avatar || 'smart_toy'}</span>
            </div>
            <div class="space-y-1" style="flex:1;">
                <div class="flex items-center gap-2 ${isRight ? 'justify-end' : ''}">
                    <span style="font-weight:700;font-size:0.875rem;">${msg.sender}</span>
                    ${modelBadge}
                    <span style="font-size:10px;color:var(--slate-400);">${msg.time}</span>
                </div>
                <div class="message-content shadow-md" style="border-radius:${isRight ? '1rem 0 1rem 1rem' : '0 1rem 1rem 1rem'};padding:1rem;border:1px solid var(--slate-200);background:white;">
                    <p id="${msgId}" style="font-size:0.875rem;color:var(--slate-800);white-space:pre-wrap;margin:0;"></p>
                </div>
            </div>
        `;

        timeline.appendChild(card);

        // Notify about new message for jump banner
        if (userScrolledUp) showJumpBanner();
        else scrollToBottom();

        requestAnimationFrame(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        });

        const textEl = card.querySelector(`#${msgId}`);

        // Non-AI messages render instantly
        const isInstant = !isAI || content.length === 0;
        if (isInstant) {
            textEl.textContent = content;
            scrollToBottom();
            if (callback) callback();
            return;
        }

        // Character-by-character stream
        let idx = 0;
        currentStreamInterval = setInterval(() => {
            if (isPaused) return; // freeze characters when paused

            textEl.textContent += content.charAt(idx);
            idx++;

            if (idx % 30 === 0) scrollToBottom();

            if (idx >= content.length) {
                clearInterval(currentStreamInterval);
                currentStreamInterval = null;
                scrollToBottom();
                if (callback) callback();
            }
        }, 12);
    }

    function renderMessage(msg) {
        // Used for immediate (non-queued) user messages
        const agentConf = AGENT_CONFIG[msg.sender] || {};
        const enriched = {
            ...msg,
            color: msg.color || agentConf.color || 'var(--slate-800)',
            avatar: msg.avatar || agentConf.icon || 'person',
            align: msg.align || agentConf.align || 'left',
        };
        streamMessage(enriched, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MESSAGE QUEUE PROCESSING
    // ─────────────────────────────────────────────────────────────────────────
    function processMessageQueue() {
        if (isPaused || isEnded || isStreaming || messageQueue.length === 0) return;

        isStreaming = true;
        if (typingIndicator) typingIndicator.style.display = 'none';

        const msg = messageQueue.shift();

        streamMessage(msg, () => {
            isStreaming = false;

            // Show typing indicator again if more messages coming
            if (!isCompletedGlobal && !isEnded && messageQueue.length === 0 && typingIndicator) {
                typingIndicator.style.display = 'flex';
                scrollToBottom();
            }

            // Apply inter-message delay
            const delay = (THROTTLE.enabled && messageQueue.length > 0) ? THROTTLE.delayMs : 0;
            setTimeout(processMessageQueue, delay);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TYPING INDICATOR
    // ─────────────────────────────────────────────────────────────────────────
    function updateTypingIndicator(isCompleted) {
        if (isCompleted || isEnded) {
            if (typingIndicator) { typingIndicator.remove(); typingIndicator = null; }
            return;
        }
        if (!typingIndicator) {
            typingIndicator = document.createElement('div');
            typingIndicator.id = 'typing-indicator';
            typingIndicator.className = 'flex gap-3 max-w-2xl mt-4';
            typingIndicator.style.opacity = '0.7';
            typingIndicator.innerHTML = `
                <div class="message-avatar" style="background:var(--slate-200);width:40px;height:40px;flex-shrink:0;display:flex;align-items:center;justify-content:center;border-radius:50%;color:var(--slate-600);">
                    <span class="material-symbols-outlined" style="animation:pulse-dot 1.5s infinite;">more_horiz</span>
                </div>
                <div class="space-y-1 py-2">
                    <p style="font-size:0.875rem;color:var(--slate-500);font-style:italic;">
                        <span style="font-weight:700;">AI agents</span> are analysing...
                    </p>
                </div>
            `;
            if (!document.getElementById('pulse-anim-style')) {
                const s = document.createElement('style');
                s.id = 'pulse-anim-style';
                s.innerHTML = '@keyframes pulse-dot{0%{opacity:.4}50%{opacity:1}100%{opacity:.4}}';
                document.head.appendChild(s);
            }
        }
        timeline.appendChild(typingIndicator);
        typingIndicator.style.display = isStreaming ? 'none' : 'flex';
        scrollToBottom();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AGENT STATUS PANEL
    // ─────────────────────────────────────────────────────────────────────────
    function updateAgentStatusPanel(msgs) {
        const counts = {};
        msgs.forEach(m => {
            const base = (m.sender || '').split(' (')[0];
            counts[base] = (counts[base] || 0) + 1;
        });

        TURN_ORDER.forEach(key => {
            const el = document.getElementById(`agent-${key.toLowerCase()}`);
            if (!el) return;
            const count      = counts[key] || 0;
            const dot        = el.querySelector('.absolute');
            const statusSpan = el.querySelectorAll('span')[1];
            const fill       = el.querySelector('.status-bar-fill');

            if (count > 0) {
                if (dot)        dot.style.background = '#22c55e';
                if (statusSpan) { statusSpan.textContent = `${count} MSGS`; statusSpan.style.color = '#22c55e'; }
                if (fill)       fill.style.width = `${Math.min(count * 33, 100)}%`;
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  POLLING
    // ─────────────────────────────────────────────────────────────────────────
    async function pollDebateStatus() {
        if (isPaused || isEnded) return;

        let retries = 0;
        while (retries < 3) {
            try {
                const token = localStorage.getItem('authToken');
                const resp = await fetch(`http://localhost:8080/api/chat/${chatSessionId}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

                const data = await resp.json();
                const updated = data.messages || [];

                if (updated.length > messages.length) {
                    const newMsgs = updated.slice(messages.length);
                    newMsgs.forEach(msg => {
                        messages.push(msg);
                        messageQueue.push(msg);
                    });
                    processMessageQueue();
                }

                isCompletedGlobal = (data.status === 'COMPLETED' || data.status === 'FAILED');
                if (isCompletedGlobal) {
                    clearInterval(pollIntervalId);
                    console.log('Debate finished:', data.status);
                }

                updateTypingIndicator(isCompletedGlobal);
                updateAgentStatusPanel(messages);
                break; // success — exit retry loop
            } catch (err) {
                retries++;
                console.warn(`Poll attempt ${retries} failed:`, err.message);
                if (retries < 3) await new Promise(r => setTimeout(r, 1500));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INJECT QUERY (human in the loop)
    // ─────────────────────────────────────────────────────────────────────────

    async function injectQuery() {
        if (!injectInput || isEnded) return;
        const text = injectInput.value.trim();
        if (!text) return;

        const now = new Date();
        const ts  = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}:${now.getSeconds().toString().padStart(2,'0')}`;

        renderMessage({ sender: 'User (Moderator)', avatar: 'person', color: '#1e293b', time: ts, content: text, align: 'right', modelName: '' });
        injectInput.value = '';

        try {
            const token = localStorage.getItem('authToken');
            await fetch(`http://localhost:8080/api/debate/${chatSessionId}/inject`, {
                method : 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body   : JSON.stringify({ text })
            });
        } catch (err) {
            console.error('Failed to inject query:', err);
            showToast('⚠️ Failed to send message — retrying on next poll.');
        }
    }

    if (injectBtn)   injectBtn.onclick = injectQuery;
    if (injectInput) injectInput.onkeypress = e => { if (e.key === 'Enter') injectQuery(); };

    // ─────────────────────────────────────────────────────────────────────────
    //  UPLOAD INTEL
    // ─────────────────────────────────────────────────────────────────────────
    const uploadBtn  = document.getElementById('upload-intel-btn');
    const fileInput  = document.getElementById('intel-file-input');

    if (uploadBtn && fileInput && projectIntelList) {
        uploadBtn.onclick = () => fileInput.click();

        fileInput.onchange = async (e) => {
            const file = e.target.files[0];
            if (!file) return;
            if (file.size > 10 * 1024 * 1024) { alert('File exceeds 10MB limit'); fileInput.value = ''; return; }

            // Guard: currentProjectId must be populated before upload
            if (!currentProjectId || currentProjectId === 'null') {
                alert('Cannot upload: project context not loaded yet. Please wait a moment and try again.');
                fileInput.value = '';
                return;
            }

            try {
                const formData = new FormData();
                formData.append('file', file);
                formData.append('projectId', currentProjectId);
                const token = localStorage.getItem('authToken');
                
                // Add a placeholder loading card
                const card = document.createElement('div');
                card.id = 'loading-upload-card';
                card.className = 'p-3 card shadow-sm';
                card.style.cssText = 'border-radius:0.75rem;border-left:4px solid var(--primary);';
                card.innerHTML = `<div style="display:flex;align-items:center;gap:8px;"><span class="pulse"></span> <span style="font-size:12px;color:var(--slate-500);">Uploading ${file.name} to project ${currentProjectId}...</span></div>`;
                projectIntelList.prepend(card);

                const res = await fetch('http://localhost:8080/api/upload', {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}` },
                    body: formData
                });
                
                if (!res.ok) {
                    const errText = await res.text().catch(() => 'Unknown error');
                    throw new Error(`Upload failed (${res.status}): ${errText}`);
                }
                
                // Refresh list
                await fetchIntelDocuments();
                
            } catch(err) {
                alert('Upload failed: ' + err.message);
                const loadingCard = document.getElementById('loading-upload-card');
                if (loadingCard) loadingCard.remove();
            }
            fileInput.value = '';
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INIT: Fetch project details + start debate
    // ─────────────────────────────────────────────────────────────────────────
    try {
        const token    = localStorage.getItem('authToken');
        const projResp = await fetch(`http://localhost:8080/api/chat/${chatSessionId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (projResp.ok) {
            const sessionData = await projResp.json();
            projectName       = sessionData.title || 'Debate';
            currentProjectId  = sessionData.projectId;
            isEnded           = sessionData.ended || sessionData.status === 'COMPLETED';

            const titleEl     = document.getElementById('debate-session-title');
            const topicEl     = document.getElementById('debate-topic-text');
            
            if (titleEl)     titleEl.textContent     = `Live Debate: ${projectName}`;
            if (topicEl)     topicEl.textContent     = `Topic: ${projectName}`;

            // Trigger Intel fetch now that we have the proper projectId
            fetchIntelDocuments();

            if (isEnded) {
                const concludeOverlay = document.getElementById('debate-concluded-overlay');
                if (concludeOverlay) concludeOverlay.style.display = 'flex';
                if (endChatBtn) endChatBtn.disabled = true;
                if (pauseBtn) pauseBtn.disabled = true;
            }
        }
    } catch (err) {
        console.warn('Could not fetch project details:', err);
    }

    // fetchIntelDocuments() is already called at line 164


    // Start the debate backend CONDITIONALLY
    try {
        const token = localStorage.getItem('authToken');
        
        // 1. Check existing status
        const statusResp = await fetch(`http://localhost:8080/api/chat/${chatSessionId}/status`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        let shouldStart = true;
        if (statusResp.ok) {
            const statusData = await statusResp.json();
            if (statusData.status !== 'NOT_STARTED' && statusData.status !== 'FAILED') {
                console.log('Debate already exists with status:', statusData.status);
                shouldStart = false; // Already IN_PROGRESS or COMPLETED
            }
        }

        // 2. Start ONLY if it doesn't exist
        if (shouldStart) {
            const resp = await fetch(`http://localhost:8080/api/chat/${chatSessionId}/start`, {
                method : 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!resp.ok) console.warn('Debate POST returned non-OK:', resp.status);
            else {
                console.log('Debate started for chatSession:', chatSessionId);
                // In a perfect system, if start returns a NEW chatSessionId from a project redirect, we'd update URL. 
                // But since we use ID interchangeably for first mount, we keep logic exactly same.
            }
        }
    } catch (err) {
        console.error('Error starting debate:', err);
    }

    // Begin polling
    pollIntervalId = setInterval(pollDebateStatus, 3000);
    pollDebateStatus();
});
