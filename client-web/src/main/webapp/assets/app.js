// Confirmation before destructive actions. Done here (not with inline onsubmit handlers) because the
// Content-Security-Policy of the application forbids inline scripts.
document.addEventListener('submit', function (event) {
  var message = event.target.getAttribute('data-confirm');
  if (message && !window.confirm(message)) {
    event.preventDefault();
  }
});
