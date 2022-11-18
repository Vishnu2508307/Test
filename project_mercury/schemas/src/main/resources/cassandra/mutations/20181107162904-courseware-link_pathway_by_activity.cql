--
-- courseware.link_pathway_by_activity
-- -----------------------------------
-- Tracks the referenced workspace activity parent pathways
--
-- Find all pathway the activity has been referenced to
-- > SELECT * FROM courseware.link_pathway_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.link_pathway_by_activity (
    activity_id timeuuid,
    pathway_id timeuuid,
    PRIMARY KEY (activity_id, pathway_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
