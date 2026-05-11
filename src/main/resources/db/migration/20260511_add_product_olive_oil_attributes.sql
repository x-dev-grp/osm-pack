-- Inventory Product olive-oil attributes.
-- The legacy skus.code column is kept as the internal product code.
-- A new skus.name column stores the product display name.

ALTER TABLE IF EXISTS skus
    ADD COLUMN IF NOT EXISTS name varchar(255);

UPDATE skus
SET name = code
WHERE name IS NULL;

ALTER TABLE IF EXISTS skus
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE IF EXISTS skus
    ADD COLUMN IF NOT EXISTS type varchar(20) NOT NULL DEFAULT 'NON_VRAC',
    ADD COLUMN IF NOT EXISTS category varchar(255),
    ADD COLUMN IF NOT EXISTS unit_of_measure varchar(30),
    ADD COLUMN IF NOT EXISTS description text,
    ADD COLUMN IF NOT EXISTS grade varchar(120),
    ADD COLUMN IF NOT EXISTS origin varchar(120),
    ADD COLUMN IF NOT EXISTS harvest_campaign varchar(40),
    ADD COLUMN IF NOT EXISTS volume real,
    ADD COLUMN IF NOT EXISTS packaging_type varchar(120),
    ADD COLUMN IF NOT EXISTS barcode varchar(120),
    ADD COLUMN IF NOT EXISTS unites_par_cols integer,
    ADD COLUMN IF NOT EXISTS colis_par_palette integer,
    ADD COLUMN IF NOT EXISTS net_weight real,
    ADD COLUMN IF NOT EXISTS gross_weight real,
    ADD COLUMN IF NOT EXISTS brand varchar(120),
    ADD COLUMN IF NOT EXISTS density real,
    ADD COLUMN IF NOT EXISTS storage_unit varchar(120),
    ADD COLUMN IF NOT EXISTS actif boolean NOT NULL DEFAULT true;

UPDATE skus
SET type = COALESCE(type, 'NON_VRAC'),
    unit_of_measure = COALESCE(unit_of_measure, CASE WHEN type = 'VRAC' THEN 'L' ELSE 'BOTTLE' END),
    actif = COALESCE(actif, true);

ALTER TABLE IF EXISTS skus
    ALTER COLUMN type SET DEFAULT 'NON_VRAC',
    ALTER COLUMN type SET NOT NULL,
    ALTER COLUMN actif SET DEFAULT true,
    ALTER COLUMN actif SET NOT NULL;

ALTER TABLE IF EXISTS skus_aud
    ADD COLUMN IF NOT EXISTS name varchar(255),
    ADD COLUMN IF NOT EXISTS type varchar(20),
    ADD COLUMN IF NOT EXISTS category varchar(255),
    ADD COLUMN IF NOT EXISTS unit_of_measure varchar(30),
    ADD COLUMN IF NOT EXISTS description text,
    ADD COLUMN IF NOT EXISTS grade varchar(120),
    ADD COLUMN IF NOT EXISTS origin varchar(120),
    ADD COLUMN IF NOT EXISTS harvest_campaign varchar(40),
    ADD COLUMN IF NOT EXISTS volume real,
    ADD COLUMN IF NOT EXISTS packaging_type varchar(120),
    ADD COLUMN IF NOT EXISTS barcode varchar(120),
    ADD COLUMN IF NOT EXISTS unites_par_cols integer,
    ADD COLUMN IF NOT EXISTS colis_par_palette integer,
    ADD COLUMN IF NOT EXISTS net_weight real,
    ADD COLUMN IF NOT EXISTS gross_weight real,
    ADD COLUMN IF NOT EXISTS brand varchar(120),
    ADD COLUMN IF NOT EXISTS density real,
    ADD COLUMN IF NOT EXISTS storage_unit varchar(120),
    ADD COLUMN IF NOT EXISTS actif boolean;

COMMENT ON TABLE skus IS 'Finished or bulk olive-oil products. Legacy table name kept for compatibility.';
COMMENT ON COLUMN skus.code IS 'Internal product code. Previously used as the SKU identifier.';
COMMENT ON COLUMN skus.name IS 'Product display name.';
COMMENT ON COLUMN skus.volume IS 'Unit volume in milliliters for NON_VRAC packaged products.';
COMMENT ON COLUMN skus.unites_par_cols IS 'Units per carton for NON_VRAC packaged products.';
COMMENT ON COLUMN skus.colis_par_palette IS 'Cartons per pallet for NON_VRAC packaged products.';
COMMENT ON COLUMN skus.density IS 'Oil density for VRAC bulk products.';
COMMENT ON COLUMN skus.storage_unit IS 'Default storage unit reference for VRAC bulk products.';
