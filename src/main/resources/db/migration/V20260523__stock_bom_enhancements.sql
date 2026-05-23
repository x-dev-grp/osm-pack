-- Optional manual migration if Hibernate ddl-auto does not apply
ALTER TABLE bom ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mouvements_stock_secs ADD COLUMN IF NOT EXISTS reference_type VARCHAR(64);
ALTER TABLE mouvements_stock_secs ADD COLUMN IF NOT EXISTS reference_id UUID;
