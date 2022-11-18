--
-- courseware.configuration_field_by_element
-- --------------------
-- Store the courseware element configuration fields on the first level of the json
--
-- Find a specific field for a courseware element
-- > SELECT * FROM courseware.configuration_field_by_element WHERE element_id = ? AND field_name = ?
--
CREATE TABLE IF NOT EXISTS courseware.configuration_field_by_element (
    element_id timeuuid,
    field_name text,
    field_value text,
    PRIMARY KEY ((element_id), field_name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
