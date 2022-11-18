--
-- courseware.theme_by_element
-- --------------------
-- This table stores themes associated to a courseware element
--
-- Fetch all themes associated to a courseware element
-- > SELECT * FROM courseware.theme_by_element WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.theme_by_element (
    element_id timeuuid,
    theme_id timeuuid,
    element_type text,
    PRIMARY KEY(element_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
