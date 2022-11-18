--
-- workspace.theme_variant
-- --------------------
-- This table stores theme variants information
--
-- Fetch all theme variants by theme id
-- > SELECT * FROM workspace.theme_variant WHERE theme_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.theme_variant (
    theme_id timeuuid,
    variant_id timeuuid,
    variant_name text,
    config text,
    primary key (theme_id, variant_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
