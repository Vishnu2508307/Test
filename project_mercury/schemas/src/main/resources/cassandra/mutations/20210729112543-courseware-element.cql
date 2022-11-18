-- courseware.element
-- --------------------
-- Keep element type by id
--
-- Find an element by id
-- > SELECT * FROM courseware.element WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.element (
    id timeuuid,
    element_type text,
    PRIMARY KEY(id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
