--
-- courseware.courseware_element_meta_information
-- ----------------------------------------------
-- Store meta information for a courseware element
--
-- Find all the meta information for a courseware element
-- > SELECT * FROM courseware.courseware_element_meta_information WHERE element_id = ?
--
-- Find a meta information for a courseware element given meta information key
-- > SELECT * FROM courseware.courseware_element_meta_information WHERE element_id = ? AND key = ?
--
CREATE TABLE IF NOT EXISTS courseware.courseware_element_meta_information (
    element_id timeuuid,
    key text,
    value text,
    element_type text,
    PRIMARY KEY ((element_id), key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
