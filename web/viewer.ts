const img = document.getElementById("frame") as HTMLImageElement;
const fpsEl = document.getElementById("fps") as HTMLElement;
const resEl = document.getElementById("res") as HTMLElement;

function updateStats() {
  fpsEl.textContent = "15";  // static demo
  resEl.textContent = img.naturalWidth + "x" + img.naturalHeight;
}

img.onload = updateStats;
window.onload = updateStats;
