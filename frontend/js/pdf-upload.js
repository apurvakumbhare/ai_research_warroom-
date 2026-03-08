/**
 * pdf-upload.js
 * Handles the landing page PDF upload zone:
 *   - Click-to-browse + Drag & Drop
 *   - Validation: PDF mime type, max 5 MB
 *   - Client-side metadata preview (name, size, type)
 *   - Animated progress bar (simulated for demo)
 *   - Accessible state transitions with ARIA live regions
 *   - Mock upload (replace with real XHR/fetch as needed)
 */

const MAX_FILE_SIZE_MB = 5;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
const ACCEPTED_MIME = 'application/pdf';

document.addEventListener('DOMContentLoaded', () => {
    // ── Element refs ──────────────────────────────────────────────────────────
    const zone = document.getElementById('upload-zone');
    const fileInput = document.getElementById('pdf-file-input');
    const uploadBtn = document.getElementById('uploadBtn');
    const clearBtn = document.getElementById('clearFileBtn');
    const submitBtn = document.getElementById('submitUploadBtn');
    const retryBtn = document.getElementById('retryBtn');

    const stateDefault = document.getElementById('state-default');
    const stateReady = document.getElementById('state-ready');
    const stateError = document.getElementById('state-error');
    const stateSuccess = document.getElementById('state-success');

    const fileNameEl = document.getElementById('file-name-display');
    const fileMetaEl = document.getElementById('file-meta-display');
    const progressBar = document.getElementById('upload-progress-bar');
    const statusText = document.getElementById('upload-status-text');
    const errorMsg = document.getElementById('error-message');

    if (!zone || !fileInput) return; // Guard: only run on the landing page

    let selectedFile = null;

    // ── State Machine ─────────────────────────────────────────────────────────
    function showState(name) {
        stateDefault.style.display = name === 'default' ? 'block' : 'none';
        stateReady.style.display = name === 'ready' ? 'flex' : 'none';
        stateError.style.display = name === 'error' ? 'flex' : 'none';
        stateSuccess.style.display = name === 'success' ? 'flex' : 'none';
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    function formatSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    }

    function setZoneDragActive(active) {
        zone.style.borderColor = active ? 'var(--primary)' : 'var(--sand-300)';
        zone.style.background = active
            ? 'rgba(167, 199, 231, 0.12)'
            : 'rgba(255,255,255,0.55)';
    }

    // ── Validation ────────────────────────────────────────────────────────────
    function validate(file) {
        if (!file) return 'No file selected.';
        if (file.type !== ACCEPTED_MIME) {
            return `Invalid file type "${file.type || 'unknown'}". Please upload a PDF.`;
        }
        if (file.size > MAX_FILE_SIZE_BYTES) {
            return `File is too large (${formatSize(file.size)}). Maximum allowed size is ${MAX_FILE_SIZE_MB} MB.`;
        }
        return null; // valid
    }

    // ── File selection handler ────────────────────────────────────────────────
    function handleFile(file) {
        const err = validate(file);
        if (err) {
            errorMsg.textContent = err;
            showState('error');
            selectedFile = null;
            return;
        }
        selectedFile = file;
        fileNameEl.textContent = file.name;
        fileMetaEl.textContent = `${formatSize(file.size)} · ${file.type}`;
        progressBar.style.width = '0%';
        statusText.textContent = 'Ready to upload.';
        showState('ready');
    }

    // ── Mock upload (simulated progress) ─────────────────────────────────────
    function mockUpload(file) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="material-symbols-outlined" style="animation:spin 1s linear infinite">sync</span> Uploading…';

        let progress = 0;
        statusText.textContent = 'Uploading…';

        const interval = setInterval(() => {
            progress += Math.random() * 18 + 5;
            if (progress >= 100) {
                progress = 100;
                clearInterval(interval);
                progressBar.style.width = '100%';
                statusText.textContent = 'Processing…';

                // Simulate server processing delay
                setTimeout(() => {
                    showState('success');
                }, 600);
            } else {
                progressBar.style.width = progress + '%';
                statusText.textContent = `Uploading… ${Math.round(progress)}%`;
            }
        }, 180);

        /**
         * ─── REAL UPLOAD (swapped in for production) ──────────────────────
         * Replace the mock above with real XHR or fetch:
         *
         * const formData = new FormData();
         * formData.append('file', file);
         * const xhr = new XMLHttpRequest();
         * xhr.open('POST', '/api/upload');
         * xhr.upload.addEventListener('progress', (e) => {
         *     if (e.lengthComputable) {
         *         const pct = Math.round((e.loaded / e.total) * 100);
         *         progressBar.style.width = pct + '%';
         *         statusText.textContent = `Uploading… ${pct}%`;
         *     }
         * });
         * xhr.addEventListener('load', () => {
         *     if (xhr.status >= 200 && xhr.status < 300) showState('success');
         *     else { errorMsg.textContent = `Server error: ${xhr.status}`; showState('error'); }
         * });
         * xhr.addEventListener('error', () => {
         *     errorMsg.textContent = 'Network error. Please check your connection.';
         *     showState('error');
         * });
         * xhr.send(formData);
         * ─────────────────────────────────────────────────────────────────
         */
    }

    // ── Event Listeners ───────────────────────────────────────────────────────

    // Open file picker via button
    uploadBtn.addEventListener('click', () => fileInput.click());

    // Keyboard activation of the zone (Enter / Space)
    zone.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            fileInput.click();
        }
    });

    // Native file input change
    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            handleFile(fileInput.files[0]);
            fileInput.value = ''; // reset so same file can be re-selected after clear
        }
    });

    // Drag events on the zone
    zone.addEventListener('dragover', (e) => {
        e.preventDefault();
        setZoneDragActive(true);
    });

    zone.addEventListener('dragleave', (e) => {
        if (!zone.contains(e.relatedTarget)) setZoneDragActive(false);
    });

    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        setZoneDragActive(false);
        const file = e.dataTransfer.files[0];
        if (file) handleFile(file);
    });

    // Clear / remove file
    clearBtn.addEventListener('click', () => {
        selectedFile = null;
        showState('default');
    });

    // Retry → back to default
    retryBtn.addEventListener('click', () => {
        selectedFile = null;
        showState('default');
    });

    // Submit / upload
    submitBtn.addEventListener('click', () => {
        if (selectedFile) mockUpload(selectedFile);
    });
});
