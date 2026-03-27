/* upload.js */
document.addEventListener('DOMContentLoaded', () => {
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('file-input');
    const form = document.getElementById('upload-form');

    if (dropzone) {
        dropzone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropzone.classList.add('drag-over');
        });

        dropzone.addEventListener('dragleave', () => {
            dropzone.classList.remove('drag-over');
        });

        dropzone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropzone.classList.remove('drag-over');
            const files = e.dataTransfer.files;
            handleFiles(files);
        });
    }

    if (fileInput) {
        fileInput.addEventListener('change', () => {
            handleFiles(fileInput.files);
        });
    }

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const projectName = document.getElementById('project-name').value;
            const coreHypothesis = document.getElementById('hypothesis').value;

            if (!projectName || !coreHypothesis) {
                alert("Please provide both Project Name and Core Hypothesis.");
                return;
            }

            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<span class="material-symbols-outlined spin" style="animation: spin 1s linear infinite;">sync</span> Launching...';
            submitBtn.disabled = true;

            try {
                // 1. Create Project
                const token = localStorage.getItem('authToken');
                const response = await fetch('http://localhost:8080/api/projects', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify({ projectName, coreHypothesis, type: 'RESEARCH' })
                });

                if (!response.ok) throw new Error('Failed to create project');
                const project = await response.json();
                const projectId = project.id;

                // Save to localStorage for debate page
                localStorage.setItem('currentProjectId', projectId);

                // 2. Upload Files
                const dropzoneFiles = document.getElementById('file-input').files;
                const intelFile = document.getElementById('intel-file-input').files[0];

                const uploadPromises = [];
                const uploadFileObj = async (file) => {
                    const formData = new FormData();
                    formData.append('file', file);
                    formData.append('projectId', projectId);
                    const uid = localStorage.getItem('uid');
                    if (uid) {
                        formData.append('uploadedBy', uid);
                    }
                    const token = localStorage.getItem('authToken');
                    return fetch(`http://localhost:8080/api/upload`, {
                        method: 'POST',
                        headers: { 'Authorization': `Bearer ${token}` },
                        body: formData
                    });
                };

                for (let file of dropzoneFiles) {
                    uploadPromises.push(uploadFileObj(file));
                }
                if (intelFile) {
                    uploadPromises.push(uploadFileObj(intelFile));
                }

                await Promise.all(uploadPromises);

                // 3. Navigate
                window.location.href = `debate.html?projectId=${projectId}`;
            } catch (err) {
                console.error(err);
                alert("Error starting project: " + err.message);
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
            }
        });
    }

    function handleFiles(files) {
        if (files.length > 0) {
            console.log(`Received ${files.length} files`);
            // Mock visual feedback
            const dropzoneText = dropzone.querySelector('h4');
            const dropzoneSubtext = dropzone.querySelector('p');
            dropzoneText.textContent = `${files.length} Files selected`;
            dropzoneSubtext.textContent = Array.from(files).map(f => f.name).join(', ');
        }
    }

    // ── Upload Intel Section Logic ──
    const intelBtn = document.getElementById('uploadIntelBtn');
    const intelInput = document.getElementById('intel-file-input');
    const intelStatus = document.getElementById('intel-attachment-status');
    const intelName = document.getElementById('intel-file-name');
    const intelMeta = document.getElementById('intel-file-meta');
    const intelError = document.getElementById('intel-file-error');
    const removeIntelBtn = document.getElementById('removeIntelBtn');

    if (intelBtn && intelInput) {
        intelBtn.addEventListener('click', () => {
            intelInput.click();
        });

        intelInput.addEventListener('change', () => {
            const file = intelInput.files[0];
            intelError.style.display = 'none';
            intelStatus.style.display = 'none';

            if (!file) return;

            // Validate size (10MB max)
            const MAX_SIZE = 10 * 1024 * 1024;
            if (file.size > MAX_SIZE) {
                intelError.textContent = 'File is too large. Maximum allowed size is 10 MB.';
                intelError.style.display = 'block';
                intelInput.value = ''; // clear
                return;
            }

            // Valid file selected
            intelName.textContent = file.name;
            const sizeKB = (file.size / 1024).toFixed(1);

            // Format mime type for display or fallback to extension
            let fileTypeDisplay = file.type || 'Unknown document';
            if (fileTypeDisplay.includes('pdf')) fileTypeDisplay = 'PDF Document';
            if (fileTypeDisplay.includes('word')) fileTypeDisplay = 'Word Document';
            if (fileTypeDisplay.includes('text')) fileTypeDisplay = 'Text File';

            intelMeta.textContent = `${sizeKB} KB · ${fileTypeDisplay}`;
            intelStatus.style.display = 'block';
        });

        if (removeIntelBtn) {
            removeIntelBtn.addEventListener('click', () => {
                intelInput.value = '';
                intelStatus.style.display = 'none';
                intelError.style.display = 'none';
            });
        }
    }
});
