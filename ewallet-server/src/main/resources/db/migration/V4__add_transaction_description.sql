ALTER TABLE trx_transactions
    ADD COLUMN IF NOT EXISTS description VARCHAR(255);
