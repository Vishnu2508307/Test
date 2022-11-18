--
-- learner.learner_element_meta_information
-- ----------------------------------------
-- Store the published meta information for a courseware element
--
-- Find all the meta information for a published element
-- > SELECT * FROM learner.learner_element_meta_information WHERE element_id = ? AND deployment_id = ? AND change_id = ?
--
-- Find a meta information for a published element given a key
-- > SELECT * FROM learner.learner_element_meta_information WHERE element_id = ? AND deployment_id = ? AND change_id = ? AND key = ?
--
CREATE TABLE IF NOT EXISTS learner.learner_element_meta_information (
    element_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    key text,
    value text,
    element_type text,
    PRIMARY KEY ((element_id, deployment_id, change_id), key)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
