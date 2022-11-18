--
-- learner.manual_grading_configuration_by_deployment
-- --------------------------------------------------
-- Stores the published version of component manual grading configurations
--
-- Find all the manually gradable components for a deployment
-- > SELECT * FROM learner.manual_grading_configuration_by_deployment WHERE deployment_id = ?
--
CREATE TABLE IF NOT EXISTS learner.manual_grading_configuration_by_deployment (
    deployment_id timeuuid,
    component_id timeuuid,
    max_score double,
    change_id timeuuid,
    parent_id timeuuid,
    parent_type text,
    PRIMARY KEY (deployment_id, component_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
