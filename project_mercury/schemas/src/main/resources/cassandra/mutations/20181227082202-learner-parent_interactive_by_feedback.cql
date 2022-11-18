--
-- learner.parent_interactive_by_feedback
-- --------------------
-- Stores the relationship between a deployed feedback and its parent interactive
--
-- Find the parent interactive for the latest deployed feedback
-- > SELECT * FROM learner.parent_interactive_by_feedback WHERE feedback_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.parent_interactive_by_feedback (
    feedback_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    interactive_id timeuuid,
    PRIMARY KEY (feedback_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
