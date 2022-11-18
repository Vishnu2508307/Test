--
-- courseware.element_description
-- ------------------------------
-- This table stores courseware element description.
--
-- Find the description of a courseware element by element id
-- > SELECT * FROM courseware.element_description WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.element_description (
    element_id timeuuid,
    element_type text,
    value text,
    PRIMARY KEY (element_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
