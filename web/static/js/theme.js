/* FinTrack — theme toggle
   Applies the saved (or system) theme immediately on load to avoid a flash,
   then wires up every .theme-toggle button on the page to switch and persist it. */
(function () {
    var STORAGE_KEY = 'fintrack-theme';
    var root = document.documentElement;

    function applyTheme(theme) {
        if (theme === 'light') {
            root.setAttribute('data-theme', 'light');
        } else {
            root.removeAttribute('data-theme');
        }
    }

    function getStoredTheme() {
        try {
            return localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            return null;
        }
    }

    function setStoredTheme(theme) {
        try {
            localStorage.setItem(STORAGE_KEY, theme);
        } catch (e) {
            /* localStorage unavailable — theme just won't persist across reloads */
        }
    }

    // Apply saved theme (or system preference) as early as possible, before paint.
    var saved = getStoredTheme();
    if (saved) {
        applyTheme(saved);
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
        applyTheme('light');
    }

    function toggleTheme() {
        var isLight = root.getAttribute('data-theme') === 'light';
        var next = isLight ? 'dark' : 'light';
        applyTheme(next);
        setStoredTheme(next);
    }

    document.addEventListener('DOMContentLoaded', function () {
        var buttons = document.querySelectorAll('.theme-toggle');
        for (var i = 0; i < buttons.length; i++) {
            buttons[i].addEventListener('click', toggleTheme);
        }
    });
})();