--
-- courseware.child_component_by_activity
-- ---------------------------------------
-- Tracks the relationship between an activity and its children components.
--
-- Find all components for an activity
-- > SELECT * FROM courseware.child_component_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.child_component_by_activity (
    activity_id timeuuid,
    component_id timeuuid,
    PRIMARY KEY (activity_id, component_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
