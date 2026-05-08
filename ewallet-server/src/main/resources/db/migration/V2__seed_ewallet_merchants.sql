INSERT INTO mst_merchants (name, created_at, updated_at)
SELECT 'Dana', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mst_merchants WHERE LOWER(name) = LOWER('Dana')
);

INSERT INTO mst_merchants (name, created_at, updated_at)
SELECT 'Gopay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mst_merchants WHERE LOWER(name) = LOWER('Gopay')
);

INSERT INTO mst_merchants (name, created_at, updated_at)
SELECT 'Shoopepay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mst_merchants WHERE LOWER(name) = LOWER('Shoopepay')
);

INSERT INTO mst_merchants (name, created_at, updated_at)
SELECT 'ovo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mst_merchants WHERE LOWER(name) = LOWER('ovo')
);

INSERT INTO mst_merchants (name, created_at, updated_at)
SELECT 'linkAja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM mst_merchants WHERE LOWER(name) = LOWER('linkAja')
);
