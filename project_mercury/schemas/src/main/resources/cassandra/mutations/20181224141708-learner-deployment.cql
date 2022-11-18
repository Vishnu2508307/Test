--
-- learner.deployment
-- --------------------
-- Store the deployment of an activity along with the change id.
--
-- Find the latest change id for a deployed activity
-- > SELECT * FROM learner.deployment WHERE id = ? and activity_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.deployment (
    id timeuuid,
    activity_id timeuuid,
    change_id timeuuid,
    PRIMARY KEY (id, activity_id, change_id)
)
WITH CLUSTERING ORDER BY (activity_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
