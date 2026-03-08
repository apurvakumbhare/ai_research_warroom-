/* report.js */
document.addEventListener('DOMContentLoaded', () => {
    console.log('Final Report loaded');

    // Animate Risk bars
    const riskFills = document.querySelectorAll('.risk-fill');
    riskFills.forEach(fill => {
        const targetWidth = fill.style.width;
        fill.style.width = '0%';
        setTimeout(() => {
            fill.style.width = targetWidth;
        }, 300);
    });

    // Animate Gauge
    const gaugeFill = document.getElementById('gauge-fill');
    if (gaugeFill) {
        // Initial offset is 37.68 (85% of 251.2 is 213.52, dashoffset = 251.2 - 213.52 = 37.68)
        const targetOffset = 37.68;
        gaugeFill.style.transition = 'stroke-dashoffset 1.5s cubic-bezier(0.4, 0, 0.2, 1)';
        gaugeFill.style.strokeDashoffset = '251.2';
        setTimeout(() => {
            gaugeFill.style.strokeDashoffset = targetOffset;
        }, 500);
    }
});
