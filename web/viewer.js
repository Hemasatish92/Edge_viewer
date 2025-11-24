const canvas = document.getElementById('frameCanvas');
const fpsEl = document.getElementById('fps');
const resEl = document.getElementById('res');

const SAMPLE_PATH = 'assets/sample_processed.png';
let useImage = false;
let sampleImg = null;

let last = performance.now();
let frames = 0;

function tryLoadSample() {
  sampleImg = new Image();
  sampleImg.src = SAMPLE_PATH;
  sampleImg.onload = function () { useImage = true; resizeCanvas(); };
  sampleImg.onerror = function () { useImage = false; };
}

function resizeCanvas() {
  if (!canvas) return;
  const maxW = Math.floor(window.innerWidth * 0.9);
  const targetW = Math.min(canvas.width, maxW);
  canvas.style.width = targetW + 'px';
}

function updateStats() {
  if (!fpsEl || !resEl || !canvas) return;
  const now = performance.now();
  const elapsed = Math.max(1, now - last);
  fpsEl.textContent = String(Math.round(frames * 1000 / elapsed));
  resEl.textContent = canvas.width + 'x' + canvas.height;
  frames = 0;
  last = now;
}

function draw() {
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const w = canvas.width;
  const h = canvas.height;

  if (useImage && sampleImg) {
    ctx.clearRect(0, 0, w, h);
    ctx.drawImage(sampleImg, 0, 0, w, h);
  } else {
    // Animated test pattern
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
  }

  frames++;
  const now = performance.now();
  if (now - last >= 1000) updateStats();

  requestAnimationFrame(draw);
}

window.addEventListener('load', function () {
  tryLoadSample();
  resizeCanvas();
  requestAnimationFrame(draw);
});

window.addEventListener('resize', resizeCanvas);
