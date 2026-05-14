DELETE FROM mst_wallets
WHERE user_id = (SELECT id FROM mst_users WHERE email = 'admin@ewallet.com');
