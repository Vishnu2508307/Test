--
-- learner.configuration_field_by_element
-- --------------------
-- Store the courseware element configuration fields on the first level of the json for a deployed element
--
-- Find all the configuration fields for a courseware element in a particular deployment
-- > SELECT * FROM learner.configuration_field_by_element WHERE deployment_id = ? AND change_id = ? AND element_id = ?
-- Find a particular configuration field for a courseware element in a particular deployment
-- > SELECT * FROM learner.configuration_field_by_element WHERE deployment_id = ? AND change_id = ? AND element_id = ? AND field_name = ?
--
CREATE TABLE IF NOT EXISTS learner.configuration_field_by_element (
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    field_name text,
    field_value text,
    PRIMARY KEY ((deployment_id, change_id, element_id), field_name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
