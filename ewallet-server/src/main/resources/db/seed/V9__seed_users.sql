-- Admin user (password: admin123)
INSERT INTO mst_users (name, email, password, role, created_at, updated_at)
SELECT 'Admin', 'admin@ewallet.com', '$2a$10$KJUsuEdCdw39reoMW.FrUutyedTjQszOpyLsQgTfmGGF5ocLWHMZy', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mst_users WHERE email = 'admin@ewallet.com'
);

INSERT INTO mst_wallets (user_id, balance, created_at, updated_at)
SELECT id, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mst_users WHERE email = 'admin@ewallet.com'
AND NOT EXISTS (
    SELECT 1 FROM mst_wallets w JOIN mst_users u ON w.user_id = u.id WHERE u.email = 'admin@ewallet.com'
);

-- Customer user (password: customer123)
INSERT INTO mst_users (name, email, password, role, created_at, updated_at)
SELECT 'Customer', 'customer@ewallet.com', '$2a$10$L4x49BkSKy5QHRwF76E3w.Un4fOmGHCwrTMC0RnTL4ni/wqMKUGxe', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mst_users WHERE email = 'customer@ewallet.com'
);

INSERT INTO mst_wallets (user_id, balance, created_at, updated_at)
SELECT id, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mst_users WHERE email = 'customer@ewallet.com'
AND NOT EXISTS (
    SELECT 1 FROM mst_wallets w JOIN mst_users u ON w.user_id = u.id WHERE u.email = 'customer@ewallet.com'
);
