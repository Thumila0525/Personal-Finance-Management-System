/* Toggles type="password" <-> type="text" for any button with
   data-toggle-password="<input id>". Shared by login.html and register.html. */
(function () {
  function eyeOff() {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a21.6 21.6 0 0 1 5.06-5.94M9.9 4.24A10.94 10.94 0 0 1 12 5c7 0 11 7 11 7a21.7 21.7 0 0 1-2.16 3.19M14.12 14.12a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';
  }
  function eyeOn() {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>';
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-toggle-password]").forEach(function (btn) {
      const input = document.getElementById(btn.dataset.togglePassword);
      if (!input) return;
      btn.addEventListener("click", function () {
        const showing = input.type === "text";
        input.type = showing ? "password" : "text";
        btn.innerHTML = showing ? eyeOn() : eyeOff();
        btn.setAttribute("aria-label", showing ? "Show password" : "Hide password");
      });
    });
  });
})();
