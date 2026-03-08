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
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            // Optional: check for project name, but navigate anyway for smooth demo
            const projectName = document.getElementById('project-name').value;
            console.log('Starting debate for project:', projectName || 'Unnamed Project');

            // Navigate to debate page after a short purely visual delay
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) submitBtn.textContent = 'Launching...';

            setTimeout(() => {
                window.location.href = 'debate.html';
            }, 500);
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
