--
-- courseware.evaluable
-- --------------------
-- This table is designed to store the evaluation mode.
--
-- Find evaluation mode of a courseware element by element id
-- > SELECT * FROM courseware.evaluable WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.evaluable (
    element_id timeuuid,
    element_type text,
    evaluation_mode text,
    PRIMARY KEY (element_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
