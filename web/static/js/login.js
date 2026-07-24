(function () {
  const form = document.getElementById("loginForm");
  const alertBox = document.getElementById("alertBox");
  const submitBtn = document.getElementById("submitBtn");

  function showAlert(message) {
    alertBox.textContent = message;
    alertBox.classList.add("show");
  }

  function hideAlert() {
    alertBox.classList.remove("show");
  }

  // If already logged in, skip straight to the dashboard.
  fetch("/api/me").then((r) => {
    if (r.ok) window.location.href = "/dashboard.html";
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    hideAlert();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    if (!email || !password) {
      showAlert("Please enter both your email and password.");
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Logging in…";

    try {
      const res = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      const data = await res.json();

      if (!res.ok) {
        showAlert(data.error || "Something went wrong. Please try again.");
        submitBtn.disabled = false;
        submitBtn.textContent = "Log in";
        return;
      }

      window.location.href = "/dashboard.html";
    } catch (err) {
      showAlert("Could not reach the server. Please try again.");
      submitBtn.disabled = false;
      submitBtn.textContent = "Log in";
    }
  });
})();
