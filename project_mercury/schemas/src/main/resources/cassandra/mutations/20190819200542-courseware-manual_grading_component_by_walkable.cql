--
-- courseware.manual_grading_component_by_walkable
-- -----------------------------------------------
-- Track manual grading components descendants by walkable
--
-- Find all the manual grading components descendants of a walkable
-- > SELECT * FROM courseware.manual_grading_component_by_walkable WHERE walkable_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.manual_grading_component_by_walkable (
    walkable_id timeuuid,
    component_id timeuuid,
    walkable_type text,
    component_parent_id timeuuid,
    component_parent_type text,
    PRIMARY KEY ((walkable_id), component_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
