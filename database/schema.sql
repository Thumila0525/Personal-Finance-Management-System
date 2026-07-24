PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL CHECK (length(trim(name)) > 0),
  email TEXT NOT NULL COLLATE NOCASE UNIQUE,
  password_hash TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS accounts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name TEXT NOT NULL CHECK (length(trim(name)) > 0),
  type TEXT NOT NULL CHECK (type IN ('BANK', 'CREDIT', 'VIRTUAL')),
  balance REAL NOT NULL DEFAULT 0 CHECK (typeof(balance) = 'real' OR typeof(balance) = 'integer'),
  credit_limit REAL NOT NULL DEFAULT 0 CHECK (credit_limit >= 0),
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
  name TEXT NOT NULL,
  UNIQUE (type, name)
);

CREATE TABLE IF NOT EXISTS transactions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
  account_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
  to_account_id INTEGER REFERENCES accounts(id) ON DELETE RESTRICT,
  category_id INTEGER REFERENCES categories(id) ON DELETE RESTRICT,
  amount REAL NOT NULL CHECK (amount > 0),
  description TEXT,
  occurred_at TEXT NOT NULL DEFAULT (datetime('now')),
  CHECK ((type = 'TRANSFER' AND to_account_id IS NOT NULL AND category_id IS NULL AND account_id <> to_account_id)
      OR (type IN ('INCOME', 'EXPENSE') AND to_account_id IS NULL AND category_id IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS budgets (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
  month TEXT NOT NULL CHECK (month GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
  monthly_limit REAL NOT NULL CHECK (monthly_limit > 0),
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE (user_id, category_id, month)
);

CREATE TABLE IF NOT EXISTS goals (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name TEXT NOT NULL CHECK (length(trim(name)) > 0),
  target_amount REAL NOT NULL CHECK (target_amount > 0),
  saved_amount REAL NOT NULL DEFAULT 0 CHECK (saved_amount >= 0),
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS goal_contributions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  goal_id INTEGER NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
  amount REAL NOT NULL CHECK (amount > 0),
  contributed_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_account ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_budgets_user_month ON budgets(user_id, month);
CREATE INDEX IF NOT EXISTS idx_goals_user ON goals(user_id);

INSERT OR IGNORE INTO categories (type, name) VALUES
  ('INCOME', 'SALARY'),
  ('INCOME', 'BONUS'),
  ('INCOME', 'GIFT'),
  ('INCOME', 'INVESTMENT'),
  ('INCOME', 'OTHER'),
  ('EXPENSE', 'GROCERIES'),
  ('EXPENSE', 'WATER_BILL'),
  ('EXPENSE', 'ELECTRIC_BILL'),
  ('EXPENSE', 'RENT'),
  ('EXPENSE', 'TRANSPORT'),
  ('EXPENSE', 'OTHER');

CREATE TRIGGER IF NOT EXISTS transactions_validate_accounts
BEFORE INSERT ON transactions
BEGIN
  SELECT CASE WHEN (SELECT user_id FROM accounts WHERE id = NEW.account_id) <> NEW.user_id
    THEN RAISE(ABORT, 'Account does not belong to user') END;
  SELECT CASE WHEN NEW.to_account_id IS NOT NULL
      AND (SELECT user_id FROM accounts WHERE id = NEW.to_account_id) <> NEW.user_id
    THEN RAISE(ABORT, 'Destination account does not belong to user') END;
END;

CREATE TRIGGER IF NOT EXISTS transactions_after_insert
AFTER INSERT ON transactions
BEGIN
  UPDATE accounts SET balance = balance + CASE WHEN NEW.type = 'INCOME' THEN NEW.amount ELSE -NEW.amount END
    WHERE id = NEW.account_id AND NEW.type <> 'TRANSFER';
  UPDATE accounts SET balance = balance - NEW.amount WHERE id = NEW.account_id AND NEW.type = 'TRANSFER';
  UPDATE accounts SET balance = balance + NEW.amount WHERE id = NEW.to_account_id AND NEW.type = 'TRANSFER';
END;

CREATE TRIGGER IF NOT EXISTS goal_contributions_after_insert
AFTER INSERT ON goal_contributions
BEGIN
  UPDATE goals SET saved_amount = saved_amount + NEW.amount WHERE id = NEW.goal_id;
END;

CREATE VIEW IF NOT EXISTS budget_progress AS
SELECT b.id, b.user_id, c.name AS category, b.month, b.monthly_limit,
       COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS spent
FROM budgets b
JOIN categories c ON c.id = b.category_id
LEFT JOIN transactions t ON t.user_id = b.user_id
  AND t.category_id = b.category_id
  AND t.type = 'EXPENSE'
  AND strftime('%Y-%m', t.occurred_at) = b.month
GROUP BY b.id;
