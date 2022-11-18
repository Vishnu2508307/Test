--
-- learner.theme_variant
-- --------------------
--This table stores selected theme variants info
--
-- Fetch theme variants info by theme id
-- > SELECT * FROM learner.theme_variant WHERE theme_id =?
--
CREATE TABLE IF NOT EXISTS learner.theme_variant (
    theme_id  timeuuid,
    variant_id  timeuuid,
    state  text,
    variant_name  text,
    config  text,
    primary key (theme_id, variant_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
