--
-- courseware.parent_pathway_by_activity
-- --------------------------------------
-- Tracks the relationship between the activity and its parent pathway
--
-- Find the parent pathway given an activity id
-- > SELECT * FROM courseware.parent_pathway_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.parent_pathway_by_activity (
    activity_id timeuuid,
    pathway_id timeuuid,
    PRIMARY KEY (activity_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
