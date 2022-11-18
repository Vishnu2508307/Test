--
-- courseware.element_by_theme
-- --------------------
-- This table is to store courseware element and themes association.
--
-- Find all courseware elements associated with a theme
-- > SELECT * FROM courseware.element_by_theme WHERE theme_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.element_by_theme (
    theme_id timeuuid,
    element_id timeuuid,
    element_type text,
    PRIMARY KEY(theme_id, element_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
