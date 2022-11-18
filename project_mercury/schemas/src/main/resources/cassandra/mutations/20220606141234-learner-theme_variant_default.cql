--
-- learner.theme_variant_default
-- --------------------
-- This table stores selected theme default variant info
--
-- Fetch default theme variants info by theme id and state
-- > SELECT * FROM learner.theme_variant_default WHERE theme_id=? AND state=?
--
CREATE TABLE IF NOT EXISTS learner.theme_variant_default (
    theme_id  timeuuid,
    state  text,
    variant_id  timeuuid,
    variant_name  text,
    config  text,
    primary key (theme_id, state)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
