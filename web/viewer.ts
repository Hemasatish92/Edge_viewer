const canvas = document.getElementById('frameCanvas') as HTMLCanvasElement | null;
const fpsEl = document.getElementById('fps') as HTMLElement | null;
const resEl = document.getElementById('res') as HTMLElement | null;

const SAMPLE_PATH = 'assets/sample_processed.png';

let useImage = false;
let sampleImg: HTMLImageElement | null = null;

function tryLoadSample() {
  sampleImg = new Image();
  sampleImg.src = SAMPLE_PATH;
  sampleImg.onload = () => { useImage = true; resizeCanvas(); };
  sampleImg.onerror = () => { useImage = false; }; // fallback to test pattern
}

function resizeCanvas() {
  if (!canvas) return;
  const maxW = Math.floor(window.innerWidth * 0.9);
  const targetW = Math.min(canvas.width, maxW);
  canvas.style.width = targetW + 'px';
}

let last = performance.now();
let frames = 0;

function updateStats() {
  if (!fpsEl || !resEl || !canvas) return;
  const now = performance.now();
  const elapsed = Math.max(1, now - last);
  fpsEl.textContent = Math.round(frames * 1000 / elapsed).toString();
  resEl.textContent = `${canvas.width}x${canvas.height}`;
  frames = 0;
  last = now;
}

function draw() {
  if (!canvas) return;
  const ctx = canvas.getContext('2d')!;
  const w = canvas.width;
  const h = canvas.height;

  if (useImage && sampleImg) {
    // Draw the sample processed image centered
    ctx.clearRect(0, 0, w, h);
    ctx.drawImage(sampleImg, 0, 0, w, h);
  } else {
    // Draw animated test pattern as fallback
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

window.addEventListener('load', () => {
  tryLoadSample();
  resizeCanvas();
  requestAnimationFrame(draw);
});

window.addEventListener('resize', resizeCanvas);
