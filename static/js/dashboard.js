(function () {
  const state = {
    accounts: [],
    categories: { income: [], expense: [] },
    chart: null,
  };

  const $ = (id) => document.getElementById(id);

  const money = (n) =>
    "Rs. " + Number(n || 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  const prettyCategory = (c) =>
    (c || "OTHER").replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (m) => m.toUpperCase());

  const timeAgo = (iso) => {
    const d = new Date(iso + "Z");
    const diffMs = Date.now() - d.getTime();
    const mins = Math.floor(diffMs / 60000);
    if (mins < 1) return "just now";
    if (mins < 60) return mins + "m ago";
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return hrs + "h ago";
    const days = Math.floor(hrs / 24);
    if (days < 7) return days + "d ago";
    return d.toLocaleDateString();
  };

  // -------------------------------------------------------------------
  // Toasts
  // -------------------------------------------------------------------
  function toast(message, isError) {
    const stack = $("toastStack");
    const el = document.createElement("div");
    el.className = "toast" + (isError ? " error" : "");
    el.textContent = message;
    stack.appendChild(el);
    setTimeout(() => el.remove(), 3500);
  }

  // -------------------------------------------------------------------
  // API helper
  // -------------------------------------------------------------------
  async function api(path, options) {
    const res = await fetch(path, {
      headers: { "Content-Type": "application/json" },
      ...options,
    });
    if (res.status === 401) {
      window.location.href = "/login.html";
      throw new Error("Not authenticated");
    }
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Something went wrong.");
    return data;
  }

  // -------------------------------------------------------------------
  // Navigation
  // -------------------------------------------------------------------
  const views = ["overview", "accounts", "transactions", "budgets", "goals"];

  function showView(name) {
    views.forEach((v) => {
      $("view-" + v).classList.toggle("hide", v !== name);
    });
    document.querySelectorAll(".nav-link[data-view]").forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.view === name);
    });
    $("sidebar").classList.remove("open");
    $("sidebarBackdrop").classList.remove("show");

    if (name === "accounts") loadAccountsView();
    if (name === "transactions") loadTransactionsView();
    if (name === "budgets") loadBudgetsView();
    if (name === "goals") loadGoalsView();
  }

  document.querySelectorAll(".nav-link[data-view]").forEach((btn) => {
    btn.addEventListener("click", () => showView(btn.dataset.view));
  });
  document.querySelectorAll("[data-nav]").forEach((btn) => {
    btn.addEventListener("click", () => showView(btn.dataset.nav));
  });

  function setSidebarOpen(open) {
    $("sidebar").classList.toggle("open", open);
    $("sidebarBackdrop").classList.toggle("show", open);
    $("menuToggle").setAttribute("aria-expanded", String(open));
  }
  $("menuToggle").addEventListener("click", () => setSidebarOpen(!$("sidebar").classList.contains("open")));
  $("sidebarBackdrop").addEventListener("click", () => setSidebarOpen(false));

  // -------------------------------------------------------------------
  // Modal helpers
  // -------------------------------------------------------------------
  function openModal(id) {
    $(id).classList.add("open");
  }
  function closeModal(id) {
    $(id).classList.remove("open");
  }
  document.querySelectorAll("[data-close]").forEach((btn) => {
    btn.addEventListener("click", () => closeModal(btn.dataset.close));
  });
  document.querySelectorAll(".modal-overlay").forEach((overlay) => {
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) overlay.classList.remove("open");
    });
  });

  // -------------------------------------------------------------------
  // Current user
  // -------------------------------------------------------------------
  async function loadMe() {
    try {
      const me = await api("/api/me");
      $("userName").textContent = me.name;
      $("userEmail").textContent = "";
      $("userAvatar").textContent = me.name.trim().charAt(0).toUpperCase();
      $("greeting").textContent = "Welcome back, " + me.name.split(" ")[0];
    } catch (e) {
      /* redirected already */
    }
  }

  $("logoutBtn").addEventListener("click", async () => {
    await api("/api/logout", { method: "POST" });
    window.location.href = "/login.html";
  });

  // -------------------------------------------------------------------
  // Categories (populate selects)
  // -------------------------------------------------------------------
  async function loadCategories() {
    const cats = await api("/api/categories");
    state.categories = cats;

    const budgetSel = $("budgetCategory");
    budgetSel.innerHTML = cats.expense.map((c) => `<option value="${c}">${prettyCategory(c)}</option>`).join("");
  }

  function populateTxCategory(type) {
    const sel = $("txCategory");
    if (type === "transfer") {
      $("txCategoryField").classList.add("hide");
      return;
    }
    $("txCategoryField").classList.remove("hide");
    const list = type === "income" ? state.categories.income : state.categories.expense;
    sel.innerHTML = list.map((c) => `<option value="${c}">${prettyCategory(c)}</option>`).join("");
  }

  // -------------------------------------------------------------------
  // Overview
  // -------------------------------------------------------------------
  async function loadOverview() {
    const statIds = ["statTotalBalance", "statIncome", "statExpense", "statNet"];
    statIds.forEach((id) => $(id).classList.add("skeleton"));

    const d = await api("/api/dashboard").finally(() => {
      statIds.forEach((id) => $(id).classList.remove("skeleton"));
    });
    state.accounts = d.accounts;

    $("statTotalBalance").textContent = money(d.totalBalance);
    $("statIncome").textContent = money(d.monthlyIncome);
    $("statExpense").textContent = money(d.monthlyExpense);
    const netEl = $("statNet");
    netEl.textContent = money(d.netThisMonth);
    netEl.className = "stat-value mono " + (d.netThisMonth >= 0 ? "positive" : "negative");

    renderRecentTx(d.recentTransactions);
    renderCategoryChart(d.expenseByCategory);
  }

  function txIconSvg(type) {
    if (type === "INCOME")
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>';
    if (type === "EXPENSE")
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>';
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>';
  }

  function renderRecentTx(list) {
    const el = $("recentTxList");
    if (!list.length) {
      el.innerHTML =
        '<div class="empty-state"><p>No transactions yet — add your first one to see it here.</p></div>';
      return;
    }
    el.innerHTML = list
      .map((t) => {
        const cls = t.type.toLowerCase();
        const sign = t.type === "EXPENSE" ? "-" : t.type === "INCOME" ? "+" : "";
        const title =
          t.type === "TRANSFER"
            ? `Transfer: ${t.accountName} → ${t.toAccountName}`
            : `${prettyCategory(t.category)} · ${t.accountName}`;
        return `
        <div class="tx-row">
          <div class="tx-badge ${cls}">${txIconSvg(t.type)}</div>
          <div class="tx-main">
            <div class="tx-title">${title}</div>
            <div class="tx-sub">${timeAgo(t.date)}</div>
          </div>
          <div class="tx-amount ${cls === "expense" ? "expense" : cls === "income" ? "income" : ""}">${sign}${money(t.amount)}</div>
        </div>`;
      })
      .join("");
  }

  function renderCategoryChart(data) {
    const canvas = $("categoryChart");
    if (!data.length) {
      canvas.classList.add("hide");
      $("categoryEmpty").classList.remove("hide");
      return;
    }
    canvas.classList.remove("hide");
    $("categoryEmpty").classList.add("hide");

    const palette = ["#45d99a", "#ff7a6f", "#e8b04b", "#6fb8ff", "#c792ea", "#8b98a6"];
    const labels = data.map((d) => prettyCategory(d.category));
    const totals = data.map((d) => d.total);

    if (state.chart) state.chart.destroy();
    state.chart = new Chart(canvas.getContext("2d"), {
      type: "doughnut",
      data: {
        labels,
        datasets: [{ data: totals, backgroundColor: palette, borderColor: "#12181f", borderWidth: 2 }],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: "bottom",
            labels: { color: "#8b98a6", font: { family: "Inter", size: 12 }, padding: 14 },
          },
        },
        cutout: "62%",
      },
    });
  }

  // -------------------------------------------------------------------
  // Accounts view
  // -------------------------------------------------------------------
  function accountIconSvg(type) {
    if (type === "CREDIT")
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" width="18" height="18"><rect x="2" y="6" width="20" height="14" rx="2.5"/><path d="M2 10h20"/><circle cx="17" cy="15" r="1.4" fill="currentColor" stroke="none"/></svg>';
    if (type === "VIRTUAL")
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" width="18" height="18"><path d="M4 7h16"/><path d="M7 7v10"/><path d="M17 7v10"/><path d="M4 17h16"/></svg>';
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" width="18" height="18"><path d="M3 10l9-6 9 6"/><path d="M5 10v9h14v-9"/><path d="M10 19v-5h4v5"/></svg>';
  }

  async function loadAccountsView() {
    const accounts = await api("/api/accounts");
    state.accounts = accounts;
    const el = $("accountsList");
    const empty = $("accountsEmpty");

    if (!accounts.length) {
      el.innerHTML = "";
      empty.classList.remove("hide");
      return;
    }
    empty.classList.add("hide");

    el.innerHTML = accounts
      .map(
        (a) => `
      <div class="account-row">
        <div class="account-info">
          <div class="account-icon ${a.type.toLowerCase()}">${accountIconSvg(a.type)}</div>
          <div>
            <div class="account-name">${escapeHtml(a.name)}</div>
            <div class="account-type">${a.type === "CREDIT" ? "Credit account" : a.type === "VIRTUAL" ? "Virtual account" : "Bank account"}</div>
          </div>
        </div>
        <div style="display:flex;align-items:center;gap:14px;">
          <div>
            <div class="account-balance">${money(a.balance)}</div>
            ${a.type === "CREDIT" ? `<div class="account-sub">Available: ${money(a.availableCredit)}</div>` : ""}
          </div>
          <div class="account-actions">
            <button class="icon-btn" data-delete-account="${a.id}" title="Delete account">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
            </button>
          </div>
        </div>
      </div>`
      )
      .join("");

    el.querySelectorAll("[data-delete-account]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        if (!confirm("Delete this account? This cannot be undone.")) return;
        try {
          await api(`/api/accounts/${btn.dataset.deleteAccount}`, { method: "DELETE" });
          toast("Account deleted.");
          loadAccountsView();
        } catch (e) {
          toast(e.message, true);
        }
      });
    });
  }

  function escapeHtml(str) {
    const d = document.createElement("div");
    d.textContent = str;
    return d.innerHTML;
  }

  $("openAddAccount").addEventListener("click", () => openModal("modalAccount"));
  $("emptyAddAccount").addEventListener("click", () => openModal("modalAccount"));

  $("accType").addEventListener("change", (e) => {
    const isCredit = e.target.value === "CREDIT";
    const isVirtual = e.target.value === "VIRTUAL";
    $("creditLimitField").classList.toggle("hide", !isCredit);
    $("virtualHint").classList.toggle("hide", !isVirtual);
  });

  $("accountForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("/api/accounts", {
        method: "POST",
        body: JSON.stringify({
          name: $("accName").value.trim(),
          type: $("accType").value,
          balance: parseFloat($("accBalance").value || "0"),
          creditLimit: parseFloat($("accCreditLimit").value || "0"),
        }),
      });
      closeModal("modalAccount");
      e.target.reset();
      $("creditLimitField").classList.add("hide");
      $("virtualHint").classList.add("hide");
      toast("Account added.");
      loadAccountsView();
    } catch (err) {
      toast(err.message, true);
    }
  });

  // -------------------------------------------------------------------
  // Transactions view
  // -------------------------------------------------------------------
  async function loadTransactionsView() {
    const list = await api("/api/transactions?limit=200");
    const el = $("txList");
    const empty = $("txEmpty");

    if (!list.length) {
      el.innerHTML = "";
      empty.classList.remove("hide");
      return;
    }
    empty.classList.add("hide");
    renderRecentTxInto(el, list);
  }

  function renderRecentTxInto(el, list) {
    el.innerHTML = list
      .map((t) => {
        const cls = t.type.toLowerCase();
        const sign = t.type === "EXPENSE" ? "-" : t.type === "INCOME" ? "+" : "";
        const title =
          t.type === "TRANSFER"
            ? `Transfer: ${t.accountName} → ${t.toAccountName}`
            : `${prettyCategory(t.category)} · ${t.accountName}`;
        return `
        <div class="tx-row">
          <div class="tx-badge ${cls}">${txIconSvg(t.type)}</div>
          <div class="tx-main">
            <div class="tx-title">${title}</div>
            <div class="tx-sub">${timeAgo(t.date)}</div>
          </div>
          <div class="tx-amount ${cls === "expense" ? "expense" : cls === "income" ? "income" : ""}">${sign}${money(t.amount)}</div>
        </div>`;
      })
      .join("");
  }

  function openAddTx() {
    if (!state.accounts.length) {
      toast("Add an account first before recording a transaction.", true);
      showView("accounts");
      return;
    }
    const opts = state.accounts.map((a) => `<option value="${a.id}">${escapeHtml(a.name)}</option>`).join("");
    $("txAccount").innerHTML = opts;
    $("txToAccount").innerHTML = opts;
    document.querySelectorAll(".tab-btn[data-tx-type]").forEach((b) => b.classList.remove("active"));
    document.querySelector('.tab-btn[data-tx-type="income"]').classList.add("active");
    setTxType("income");
    openModal("modalTx");
  }

  $("openAddTx").addEventListener("click", openAddTx);
  $("fabAdd").addEventListener("click", openAddTx);

  function setTxType(type) {
    $("txForm").dataset.type = type;
    $("txFromLabel").textContent = type === "expense" ? "Account to withdraw from" : type === "transfer" ? "From account" : "Deposit into account";
    $("txToField").classList.toggle("hide", type !== "transfer");
    populateTxCategory(type);
  }

  document.querySelectorAll(".tab-btn[data-tx-type]").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.querySelectorAll(".tab-btn[data-tx-type]").forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      setTxType(btn.dataset.txType);
    });
  });

  $("txForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const type = e.target.dataset.type || "income";
    const amount = parseFloat($("txAmount").value);
    try {
      if (type === "income") {
        await api("/api/transactions/income", {
          method: "POST",
          body: JSON.stringify({ accountId: parseInt($("txAccount").value), amount, category: $("txCategory").value }),
        });
      } else if (type === "expense") {
        await api("/api/transactions/expense", {
          method: "POST",
          body: JSON.stringify({ accountId: parseInt($("txAccount").value), amount, category: $("txCategory").value }),
        });
      } else {
        await api("/api/transactions/transfer", {
          method: "POST",
          body: JSON.stringify({
            fromAccountId: parseInt($("txAccount").value),
            toAccountId: parseInt($("txToAccount").value),
            amount,
          }),
        });
      }
      closeModal("modalTx");
      e.target.reset();
      toast("Transaction saved.");
      refreshCurrentView();
    } catch (err) {
      toast(err.message, true);
    }
  });

  // -------------------------------------------------------------------
  // Budgets view
  // -------------------------------------------------------------------
  async function loadBudgetsView() {
    const list = await api("/api/budgets");
    const el = $("budgetsList");
    const empty = $("budgetsEmpty");

    if (!list.length) {
      el.innerHTML = "";
      empty.classList.remove("hide");
      return;
    }
    empty.classList.add("hide");

    el.innerHTML = list
      .map((b) => {
        const pct = Math.min(100, (b.spent / b.monthlyLimit) * 100);
        const over = b.spent > b.monthlyLimit;
        const warn = !over && pct >= 80;
        return `
        <div class="budget-item">
          <div class="budget-head">
            <span class="name">${prettyCategory(b.category)}</span>
            <span class="amounts">${money(b.spent)} / ${money(b.monthlyLimit)}</span>
          </div>
          <div class="progress-track">
            <div class="progress-fill ${over ? "over" : warn ? "warn" : ""}" style="width:${pct}%"></div>
          </div>
        </div>`;
      })
      .join("");
  }

  $("openAddBudget").addEventListener("click", () => openModal("modalBudget"));
  $("emptyAddBudget").addEventListener("click", () => openModal("modalBudget"));

  $("budgetForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("/api/budgets", {
        method: "POST",
        body: JSON.stringify({ category: $("budgetCategory").value, monthlyLimit: parseFloat($("budgetLimit").value) }),
      });
      closeModal("modalBudget");
      e.target.reset();
      toast("Budget saved.");
      loadBudgetsView();
    } catch (err) {
      toast(err.message, true);
    }
  });

  // -------------------------------------------------------------------
  // Goals view
  // -------------------------------------------------------------------
  async function loadGoalsView() {
    const list = await api("/api/goals");
    const el = $("goalsList");
    const empty = $("goalsEmpty");

    if (!list.length) {
      el.innerHTML = "";
      empty.classList.remove("hide");
      return;
    }
    empty.classList.add("hide");

    el.innerHTML = list
      .map((g) => {
        const pct = Math.min(100, (g.savedAmount / g.targetAmount) * 100);
        return `
        <div class="goal-item">
          <div class="budget-head">
            <span class="name">${escapeHtml(g.name)}</span>
            <span class="amounts">${money(g.savedAmount)} / ${money(g.targetAmount)}</span>
          </div>
          <div class="progress-track"><div class="progress-fill" style="width:${pct}%"></div></div>
          <div style="display:flex;gap:8px;margin-top:10px;">
            <button class="btn btn-ghost btn-sm" data-contribute="${g.id}">+ Add funds</button>
            <button class="btn btn-danger btn-sm" data-delete-goal="${g.id}">Delete</button>
          </div>
        </div>`;
      })
      .join("");

    el.querySelectorAll("[data-contribute]").forEach((btn) => {
      btn.addEventListener("click", () => {
        $("contributeGoalId").value = btn.dataset.contribute;
        openModal("modalContribute");
      });
    });
    el.querySelectorAll("[data-delete-goal]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        if (!confirm("Delete this savings goal?")) return;
        try {
          await api(`/api/goals/${btn.dataset.deleteGoal}`, { method: "DELETE" });
          toast("Goal deleted.");
          loadGoalsView();
        } catch (err) {
          toast(err.message, true);
        }
      });
    });
  }

  $("openAddGoal").addEventListener("click", () => openModal("modalGoal"));
  $("emptyAddGoal").addEventListener("click", () => openModal("modalGoal"));

  $("goalForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("/api/goals", {
        method: "POST",
        body: JSON.stringify({ name: $("goalName").value.trim(), targetAmount: parseFloat($("goalTarget").value) }),
      });
      closeModal("modalGoal");
      e.target.reset();
      toast("Goal created.");
      loadGoalsView();
    } catch (err) {
      toast(err.message, true);
    }
  });

  $("contributeForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api(`/api/goals/${$("contributeGoalId").value}/contribute`, {
        method: "POST",
        body: JSON.stringify({ amount: parseFloat($("contributeAmount").value) }),
      });
      closeModal("modalContribute");
      e.target.reset();
      toast("Contribution added.");
      loadGoalsView();
    } catch (err) {
      toast(err.message, true);
    }
  });

  // -------------------------------------------------------------------
  // Refresh whichever view is currently active
  // -------------------------------------------------------------------
  function refreshCurrentView() {
    const active = document.querySelector(".nav-link.active");
    const name = active ? active.dataset.view : "overview";
    if (name === "overview") loadOverview();
    else showView(name);
    // Overview stats should stay fresh regardless of which view triggered the change.
    if (name !== "overview") loadOverview();
  }

  // -------------------------------------------------------------------
  // Init
  // -------------------------------------------------------------------
  (async function init() {
    await loadMe();
    await loadCategories();
    await loadOverview();
    await api("/api/accounts").then((a) => (state.accounts = a));
  })();
})();
