CREATE TYPE transaction_type AS ENUM ('TOPUP', 'PAYMENT', 'TRANSFER');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED');

CREATE TABLE mst_users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mst_wallets (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    balance BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES mst_users(id) ON DELETE CASCADE
);

CREATE TABLE mst_merchants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trx_transactions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    merchant_id INT,
    amount BIGINT NOT NULL,
    type transaction_type NOT NULL,
    status transaction_status NOT NULL,
    reference_id VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trx_user FOREIGN KEY (user_id) REFERENCES mst_users(id),
    CONSTRAINT fk_trx_merchant FOREIGN KEY (merchant_id) REFERENCES mst_merchants(id)
);

CREATE INDEX idx_transactions_user_id ON trx_transactions(user_id);
CREATE INDEX idx_transactions_merchant_id ON trx_transactions(merchant_id);
CREATE INDEX idx_transactions_created_at ON trx_transactions(created_at);

CREATE INDEX idx_transactions_user_status_date 
ON trx_transactions(user_id, status, created_at DESC);