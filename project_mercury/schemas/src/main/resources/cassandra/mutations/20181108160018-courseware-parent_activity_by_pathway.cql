--
-- courseware.parent_activity_by_pathway
-- --------------------------------------
-- Store the relationship between a pathway and its parent activity.
--
-- Find the pathway parent activity
-- > SELECT * FROM courseware.parent_activity_by_pathway WHERE pathway_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.parent_activity_by_pathway (
    pathway_id timeuuid,
    activity_id timeuuid,
    PRIMARY KEY (pathway_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
