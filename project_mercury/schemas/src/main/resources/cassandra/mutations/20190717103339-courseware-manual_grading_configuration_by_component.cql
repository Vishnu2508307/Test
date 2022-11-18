--
-- courseware.manual_grading_configuration_by_component
-- ----------------------------------------------------
-- Stores manual grading configuration for components
--
-- Find the manual grading configurations for a component
-- > SELECT * FROM courseware.manual_grading_configuration_by_component WHERE element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.manual_grading_configuration_by_component (
    component_id timeuuid,
    max_score double,
    PRIMARY KEY (component_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
