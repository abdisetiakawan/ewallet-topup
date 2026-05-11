CREATE TYPE idempotency_status AS ENUM ('PROCESSING', 'COMPLETED', 'FAILED');

CREATE TABLE trx_idempotency_keys (
    id SERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    request_hash VARCHAR(255) NOT NULL,
    status idempotency_status NOT NULL,
    http_status INT,
    response_body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_idempotency_scope UNIQUE (user_id, endpoint, idempotency_key)
);

CREATE INDEX idx_idempotency_expires_at ON trx_idempotency_keys(expires_at);
