--
-- learner.manual_grading_component_by_walkable
-- --------------------------------------------
-- Track all the manual grading components descendant of a walkable
--
-- Find all the manual grading component descendants of a walkable
-- > SELECT * FROM learner.manual_grading_component_by_walkable WHERE ...
--
CREATE TABLE IF NOT EXISTS learner.manual_grading_component_by_walkable (
    deployment_id timeuuid,
    walkable_id timeuuid,
    component_id timeuuid,
    change_id timeuuid,
    walkable_type text,
    component_parent_id timeuuid,
    component_parent_type text,
    PRIMARY KEY ((deployment_id, walkable_id), component_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
