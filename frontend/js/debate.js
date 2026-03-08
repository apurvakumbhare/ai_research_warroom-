/* debate.js - Simulates live AI debate messages */

const messages = [
    {
        sender: "Researcher-01",
        avatar: "search_insights",
        color: "var(--ocean)",
        time: "14:02:11",
        content: "Based on the recent scaling data, I propose that we prioritize model size over immediate interpretability layers. The performance gap is widening too rapidly to ignore.",
        align: "left"
    },
    {
        sender: "Critic-Alpha",
        avatar: "gavel",
        color: "var(--driftwood)",
        time: "14:02:45",
        content: "I disagree. Scaling without interpretability is a safety debt. We found 4 cases in the Intel logs where black-box models failed silently in edge cases.",
        align: "right"
    },
    {
        sender: "Optimizer-X",
        avatar: "bolt",
        color: "var(--primary)",
        time: "14:03:12",
        content: "Could we meet in the middle? Use a 70/30 split of compute for parameter scaling vs attention-head visualization. I've calculated the Pareto frontier for this approach.",
        align: "left"
    },
    {
        sender: "Devil's Advocate",
        avatar: "psychology",
        color: "var(--driftwood)",
        time: "14:04:02",
        content: "What if both approaches are wrong? If the underlying architecture is flawed, scaling only amplifies the error. We need to look at sparse activation routes.",
        align: "right"
    }
];

document.addEventListener('DOMContentLoaded', () => {
    const timeline = document.getElementById('timeline-container');
    let messageIndex = 0;
    let isPaused = false;
    let simulationTimeoutId = null;

    function addMessage() {
        if (messageIndex >= messages.length) return;

        const msg = messages[messageIndex];
        const card = document.createElement('div');
        card.className = `flex gap-4 max-w-2xl ${msg.align === 'right' ? 'ml-auto flex-row-reverse text-right' : ''}`;
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s ease';

        const avatarHtml = `
            <div class="message-avatar" style="background: ${msg.color}; width: 40px; height: 40px;">
                <span class="material-symbols-outlined">${msg.avatar}</span>
            </div>
        `;

        const contentHtml = `
            <div class="space-y-1">
                <div class="flex items-center gap-2 ${msg.align === 'right' ? 'justify-end' : ''}">
                    <span style="font-weight: 700; font-size: 0.875rem;">${msg.sender}</span>
                    <span style="font-size: 10px; color: var(--slate-400);">${msg.time}</span>
                </div>
                <div class="message-content shadow-md" style="border-radius: ${msg.align === 'right' ? '1rem 0 1rem 1rem' : '0 1rem 1rem 1rem'};">
                    <p style="font-size: 0.875rem; color: var(--slate-800);">${msg.content}</p>
                </div>
            </div>
        `;

        card.innerHTML = avatarHtml + contentHtml;
        timeline.appendChild(card);

        // Trigger animation
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';

            // Scroll to bottom
            const timelineContainer = document.getElementById('debate-timeline');
            if (timelineContainer) {
                timelineContainer.scrollTo({
                    top: timelineContainer.scrollHeight,
                    behavior: 'smooth'
                });
            }
        }, 100);

        messageIndex++;

        // Schedule next message only if not paused
        if (messageIndex < messages.length && !isPaused) {
            simulationTimeoutId = setTimeout(addMessage, 3000 + Math.random() * 2000);
        }
    }

    // Start simulation after delay
    simulationTimeoutId = setTimeout(addMessage, 1000);

    // --- Feature 1: Pause Button Logic ---
    const pauseBtn = document.getElementById('pause-btn');
    if (pauseBtn) {
        pauseBtn.addEventListener('click', () => {
            isPaused = !isPaused;
            if (isPaused) {
                pauseBtn.textContent = 'RESUME';
                pauseBtn.classList.replace('btn-secondary', 'btn-primary');
                // Stop the simulation
                if (simulationTimeoutId) {
                    clearTimeout(simulationTimeoutId);
                }
            } else {
                pauseBtn.textContent = 'PAUSE';
                pauseBtn.classList.replace('btn-primary', 'btn-secondary');
                // Resume simulation
                if (messageIndex < messages.length) {
                    simulationTimeoutId = setTimeout(addMessage, 1000);
                }
            }
        });
    }

    // --- Feature 2: Inject Query Logic ---
    const injectInput = document.getElementById('inject-input');
    const injectBtn = document.getElementById('inject-btn');

    async function injectQuery() {
        if (!injectInput) return;
        const text = injectInput.value.trim();
        if (!text) return;

        const now = new Date();
        const timeString = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;

        const userMsg = {
            sender: "User",
            avatar: "person",
            color: "var(--slate-800)",
            time: timeString,
            content: text,
            align: "right"
        };

        // Optimistic UI Update: We temporarily push it to messages array and render immediately
        // Note: For a real app, we'd render it distinctly and update its status upon API success

        // Render Immediately using the existing helper structure (simplified)
        const card = document.createElement('div');
        card.className = `flex gap-4 max-w-2xl ml-auto flex-row-reverse text-right`;
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s ease';

        const avatarHtml = `
            <div class="message-avatar" style="background: ${userMsg.color}; width: 40px; height: 40px;">
                <span class="material-symbols-outlined">${userMsg.avatar}</span>
            </div>
        `;

        const contentHtml = `
            <div class="space-y-1">
                <div class="flex items-center gap-2 justify-end">
                    <span style="font-weight: 700; font-size: 0.875rem;">${userMsg.sender}</span>
                    <span style="font-size: 10px; color: var(--slate-400);">${userMsg.time}</span>
                </div>
                <div class="message-content shadow-md" style="border-radius: 1rem 0 1rem 1rem; border: 1px solid var(--slate-200); background: white;">
                    <p style="font-size: 0.875rem; color: var(--slate-800);">${userMsg.content}</p>
                </div>
            </div>
        `;

        card.innerHTML = avatarHtml + contentHtml;
        timeline.appendChild(card);

        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
            const timelineContainer = document.getElementById('debate-timeline');
            timelineContainer.scrollTo({
                top: timelineContainer.scrollHeight,
                behavior: 'smooth'
            });
        }, 100);

        injectInput.value = '';

        // API Call Simulation
        try {
            const payload = { sender: "user", text: text, timestamp: now.toISOString() };
            console.log('API Payload Sent:', payload);
        } catch (error) {
            console.error('Failed to inject query', error);
        }
    }

    if (injectBtn) {
        console.log("Inject button found, adding listener");
        // Ensure to remove any existing listeners if this script runs multiple times
        injectBtn.onclick = injectQuery;
    } else {
        console.error("Inject button not found in DOM");
    }

    if (injectInput) {
        injectInput.onkeypress = (e) => {
            if (e.key === 'Enter') {
                injectQuery();
            }
        };
    } else {
        console.error("Inject input not found in DOM");
    }

    // --- Feature 3: Project Intel Upload Logic ---
    const uploadBtn = document.getElementById('upload-intel-btn');
    const fileInput = document.getElementById('intel-file-input');
    const intelList = document.getElementById('project-intel-list');

    if (uploadBtn && fileInput && intelList) {
        console.log("Upload elements found, adding listeners");
        // Using onclick to guarantee binding instead of addEventListener which can stack
        uploadBtn.onclick = () => {
            console.log("Upload button clicked, opening file dialog");
            fileInput.click();
        };

        // Ensure we only have one change listener
        fileInput.onchange = async (e) => {
            console.log("File selected");
            const files = e.target.files;
            if (files.length === 0) {
                alert("No file selected");
                return;
            }

            const file = files[0];

            // 1. Validation (10MB limit)
            if (file.size > 10 * 1024 * 1024) {
                alert('File exceeds 10MB limit');
                fileInput.value = '';
                return;
            }

            // 2. Determine generic type for badge
            let fileTypeDisplay = 'DOCUMENT';
            if (file.name.endsWith('.pdf')) fileTypeDisplay = 'PDF';
            else if (file.name.endsWith('.txt')) fileTypeDisplay = 'TEXT';
            else if (file.name.endsWith('.docx') || file.name.endsWith('.doc')) fileTypeDisplay = 'WORD';
            else if (file.name.endsWith('.png') || file.name.endsWith('.jpg') || file.name.endsWith('.jpeg')) fileTypeDisplay = 'IMAGE';

            const now = new Date();
            const timeString = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;

            // 3. Create actual card immediately (Optimistic local-only UI)
            const realCard = document.createElement('div');
            realCard.className = 'p-3 card shadow-sm';
            realCard.style.borderRadius = '0.75rem';
            realCard.style.borderLeft = '4px solid var(--primary)';
            realCard.innerHTML = `
                <div class="flex justify-between mb-1">
                    <span class="badge" style="background: var(--ocean); color: white; font-size: 10px;">${fileTypeDisplay}</span>
                    <span style="font-size: 10px; color: var(--slate-400);">Uploaded at ${timeString}</span>
                </div>
                <h4 style="font-size: 0.875rem; font-weight: 700;" title="${file.name}">${file.name}</h4>
                <p style="font-size: 0.75rem; color: var(--slate-500); display: flex; align-items: center; gap: 4px;">
                    <span class="material-symbols-outlined" style="font-size: 14px; color: #22c55e;">check_circle</span> File added to Project Intel
                </p>
            `;

            intelList.prepend(realCard);

            // Reset input so the same file can be uploaded again if needed
            fileInput.value = '';
        };
    } else {
        console.error("Upload elements missing from DOM:", { uploadBtn, fileInput, intelList });
    }
});
