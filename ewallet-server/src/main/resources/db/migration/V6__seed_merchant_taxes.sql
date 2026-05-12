-- Seed Tax for 'Dana' (FIXED - Rp 1.000)
INSERT INTO mst_merchant_taxes (merchant_id, tax_name, tax_type, tax_value, is_active, effective_at, created_at, updated_at)
SELECT id, 'Admin Fee', 'FIXED', 1000.0000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mst_merchants WHERE LOWER(name) = LOWER('Dana')
AND NOT EXISTS (
    SELECT 1 FROM mst_merchant_taxes mt
    JOIN mst_merchants m ON mt.merchant_id = m.id
    WHERE LOWER(m.name) = LOWER('Dana') AND mt.tax_name = 'Admin Fee'
);

-- Seed Tax for 'Gopay' (PERCENTAGE - 1.5%)
INSERT INTO mst_merchant_taxes (merchant_id, tax_name, tax_type, tax_value, is_active, effective_at, created_at, updated_at)
SELECT id, 'Service Fee', 'PERCENTAGE', 1.5000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mst_merchants WHERE LOWER(name) = LOWER('Gopay')
AND NOT EXISTS (
    SELECT 1 FROM mst_merchant_taxes mt
    JOIN mst_merchants m ON mt.merchant_id = m.id
    WHERE LOWER(m.name) = LOWER('Gopay') AND mt.tax_name = 'Service Fee'
);

-- Seed Tax for 'Shoopepay' (FIXED - Rp 500)
INSERT INTO mst_merchant_taxes (merchant_id, tax_name, tax_type, tax_value, is_active, effective_at, created_at, updated_at)
SELECT id, 'Processing Fee', 'FIXED', 500.0000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mst_merchants WHERE LOWER(name) = LOWER('Shoopepay')
AND NOT EXISTS (
    SELECT 1 FROM mst_merchant_taxes mt
    JOIN mst_merchants m ON mt.merchant_id = m.id
    WHERE LOWER(m.name) = LOWER('Shoopepay') AND mt.tax_name = 'Processing Fee'
);

-- Seed Tax for 'ovo' (FIXED - Rp 1.500)
INSERT INTO mst_merchant_taxes (merchant_id, tax_name, tax_type, tax_value, is_active, effective_at, created_at, updated_at)
SELECT id, 'Platform Fee', 'FIXED', 1500.0000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mst_merchants WHERE LOWER(name) = LOWER('ovo')
AND NOT EXISTS (
    SELECT 1 FROM mst_merchant_taxes mt
    JOIN mst_merchants m ON mt.merchant_id = m.id
    WHERE LOWER(m.name) = LOWER('ovo') AND mt.tax_name = 'Platform Fee'
);

-- Seed Tax for 'linkAja' (PERCENTAGE - 2.0%)
INSERT INTO mst_merchant_taxes (merchant_id, tax_name, tax_type, tax_value, is_active, effective_at, created_at, updated_at)
SELECT id, 'Transfer Fee', 'PERCENTAGE', 2.0000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM mst_merchants WHERE LOWER(name) = LOWER('linkAja')
AND NOT EXISTS (
    SELECT 1 FROM mst_merchant_taxes mt
    JOIN mst_merchants m ON mt.merchant_id = m.id
    WHERE LOWER(m.name) = LOWER('linkAja') AND mt.tax_name = 'Transfer Fee'
);
