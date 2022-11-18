--
-- courseware.pathway_by_activity
-- ------------------------------
-- Track all the pathways for a given activity
--
-- Find the pathways for a given activity id
-- > SELECT * FROM courseware.pathway_by_activity WHERE activity_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.pathway_by_activity (
    activity_id timeuuid,
    pathway_id timeuuid,
    PRIMARY KEY(activity_id, pathway_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};

