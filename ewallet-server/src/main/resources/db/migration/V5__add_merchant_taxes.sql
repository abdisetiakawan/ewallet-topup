CREATE TYPE tax_type_enum AS ENUM ('PERCENTAGE', 'FIXED');

ALTER TABLE mst_merchants ADD COLUMN is_active BOOLEAN DEFAULT true;

CREATE TABLE mst_merchant_taxes (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES mst_merchants(id),
    tax_name VARCHAR(100) NOT NULL,
    tax_type tax_type_enum NOT NULL,
    tax_value DECIMAL(10, 4) NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    expired_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE trx_transactions ADD COLUMN base_amount BIGINT;
ALTER TABLE trx_transactions ADD COLUMN tax_amount BIGINT DEFAULT 0;
ALTER TABLE trx_transactions ADD COLUMN tax_snapshot JSONB;

-- For existing records, set base_amount to amount and tax_amount to 0
UPDATE trx_transactions SET base_amount = amount, tax_amount = 0;

-- Now enforce NOT NULL constraints
ALTER TABLE trx_transactions ALTER COLUMN base_amount SET NOT NULL;
ALTER TABLE trx_transactions ALTER COLUMN tax_amount SET NOT NULL;
