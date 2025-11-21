const canvas = document.getElementById("frameCanvas");
const fpsEl = document.getElementById("fps");
const resEl = document.getElementById("res");

let last = performance.now();
let frames = 0;

function updateStats() {
  if (!fpsEl || !resEl || !canvas) return;
  fpsEl.textContent = Math.round(frames * 1000 / Math.max(1, (performance.now() - last))) || "--";
  resEl.textContent = canvas.width + "x" + canvas.height;
}

function drawTestPattern() {
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  // animate gradient/pattern
  const t = performance.now() / 1000;
  const imgData = ctx.createImageData(w, h);
  let i = 0;
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const r = Math.floor(128 + 127 * Math.sin((x / w) * 6.28 + t));
      const g = Math.floor(128 + 127 * Math.sin((y / h) * 6.28 + t * 1.3));
      const b = Math.floor(128 + 127 * Math.sin(((x + y) / (w + h)) * 6.28 + t * 0.7));
      imgData.data[i++] = r;
      imgData.data[i++] = g;
      imgData.data[i++] = b;
      imgData.data[i++] = 255;
    }
  }
  ctx.putImageData(imgData, 0, 0);

  frames++;
  const now = performance.now();
  if (now - last >= 1000) {
    updateStats();
    frames = 0;
    last = now;
  }

  requestAnimationFrame(drawTestPattern);
}

window.addEventListener('load', () => {
  // Resize canvas to fit container while keeping ratio
  const container = canvas.parentElement;
  if (container) {
    const maxW = Math.floor(window.innerWidth * 0.9);
    const targetW = Math.min(canvas.width, maxW);
    canvas.style.width = targetW + 'px';
  }
  requestAnimationFrame(drawTestPattern);
});
