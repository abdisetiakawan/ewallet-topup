-- 1. Rename existing tax_type column to value_type
ALTER TABLE mst_merchant_taxes RENAME COLUMN tax_type TO value_type;

-- 2. Rename the enum type used by value_type
ALTER TYPE tax_type_enum RENAME TO tax_value_type_enum;

-- 3. Create the new tax_type_enum for categories
CREATE TYPE tax_type_enum AS ENUM ('PPN', 'SERVICE_FEE', 'PLATFORM_FEE', 'ADMIN_FEE', 'TRANSFER_FEE', 'PROCESSING_FEE');

-- 4. Add the new tax_type column
ALTER TABLE mst_merchant_taxes ADD COLUMN tax_type tax_type_enum;

-- 5. Backfill the new tax_type column based on data from V6 seeder
UPDATE mst_merchant_taxes SET tax_type = 'ADMIN_FEE' WHERE tax_name = 'Admin Fee';
UPDATE mst_merchant_taxes SET tax_type = 'SERVICE_FEE' WHERE tax_name = 'Service Fee';
UPDATE mst_merchant_taxes SET tax_type = 'PROCESSING_FEE' WHERE tax_name = 'Processing Fee';
UPDATE mst_merchant_taxes SET tax_type = 'PLATFORM_FEE' WHERE tax_name = 'Platform Fee';
UPDATE mst_merchant_taxes SET tax_type = 'TRANSFER_FEE' WHERE tax_name = 'Transfer Fee';

-- If there are any unmapped rows, give them a default value (fallback)
UPDATE mst_merchant_taxes SET tax_type = 'ADMIN_FEE' WHERE tax_type IS NULL;

-- 6. Enforce NOT NULL on the new tax_type column
ALTER TABLE mst_merchant_taxes ALTER COLUMN tax_type SET NOT NULL;

-- 7. Add partial unique constraint: max 1 active tax category per merchant
CREATE UNIQUE INDEX uq_merchant_active_tax ON mst_merchant_taxes (merchant_id, tax_type) WHERE is_active = true;
