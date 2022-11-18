--
-- courseware.element_by_theme
-- --------------------
-- This table stores element associated to a courseware theme
--
-- Fetch all element associated to a courseware theme
-- > SELECT * FROM courseware.element_by_theme WHERE theme_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.element_by_theme (
    theme_id timeuuid,
    element_id timeuuid,
     element_type text,
    PRIMARY KEY(theme_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
