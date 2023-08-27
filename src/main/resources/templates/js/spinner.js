const loadingOverlay = document.createElement("div");
loadingOverlay.id = "loading-indicator-overlay";

const loadingIndicator = document.createElement("div");
loadingIndicator.id = "loading-indicator";

const spinner = document.createElement("div");
spinner.classList.add("spinner");


const span = document.createElement("span");

loadingIndicator.appendChild(spinner);
loadingIndicator.appendChild(span);
loadingOverlay.appendChild(loadingIndicator);
document.body.appendChild(loadingOverlay);
function showSpinningIndicator(text) {
    span.textContent = text;
    loadingOverlay.style.display = 'flex';
}
function hideSpinningIndicator() {
    loadingOverlay.style.display = 'none';
}