--
-- courseware.child_pathway_by_activity
-- -------------------------------------
-- Tracks the relationship between an activity and its pathway children.
--
-- Find all the pathway children for an activity
-- > SELECT * FROM courseware.child_pathway_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.child_pathway_by_activity (
    activity_id timeuuid,
    pathway_ids list<timeuuid>,
    PRIMARY KEY (activity_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
