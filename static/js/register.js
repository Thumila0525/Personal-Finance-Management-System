(function () {
  const form = document.getElementById("registerForm");
  const alertBox = document.getElementById("alertBox");
  const submitBtn = document.getElementById("submitBtn");

  function showAlert(message) {
    alertBox.textContent = message;
    alertBox.classList.add("show");
  }

  function hideAlert() {
    alertBox.classList.remove("show");
  }

  fetch("/api/me").then((r) => {
    if (r.ok) window.location.href = "/dashboard.html";
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    hideAlert();

    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (!name || !email || !password) {
      showAlert("Please fill in every field.");
      return;
    }
    if (password.length < 6) {
      showAlert("Your password needs to be at least 6 characters.");
      return;
    }
    if (password !== confirmPassword) {
      showAlert("Those passwords don't match. Please try again.");
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Creating account…";

    try {
      const res = await fetch("/api/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, password }),
      });
      const data = await res.json();

      if (!res.ok) {
        showAlert(data.error || "Something went wrong. Please try again.");
        submitBtn.disabled = false;
        submitBtn.textContent = "Create account";
        return;
      }

      window.location.href = "/dashboard.html";
    } catch (err) {
      showAlert("Could not reach the server. Please try again.");
      submitBtn.disabled = false;
      submitBtn.textContent = "Create account";
    }
  });
})();
