--
-- learner.theme_by_element
-- --------------------
-- This table stores selected theme variants by element id
--
-- Fetch selected theme info by element_id
-- > SELECT * FROM learner.theme_by_element WHERE element_id=?
--
CREATE TABLE IF NOT EXISTS learner.theme_by_element (
    element_id  timeuuid,
    theme_id  timeuuid,
    theme_name  text,
    primary key (element_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
