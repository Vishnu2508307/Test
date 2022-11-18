--
-- workspace.theme_variant_by_state
-- --------------------
-- This table stores theme variants by state information
--
-- Fetch all theme variants by theme id nad state
-- > SELECT * FROM workspace.theme_variant_by_state WHERE theme_id = ? and state = ?
--
CREATE TABLE IF NOT EXISTS workspace.theme_variant_by_state (
    theme_id timeuuid,
    state text,
    variant_name text,
    config text,
    primary key (theme_id, state)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
