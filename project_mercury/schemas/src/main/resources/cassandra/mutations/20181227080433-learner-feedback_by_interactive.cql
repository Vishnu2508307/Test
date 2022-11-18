--
-- learner.feedback_by_interactive
-- --------------------
-- Stores the feedback for a deployed interactive
--
-- Find all feedback for a deployed interactive
-- > SELECT * FROM learner.feedback_by_interactive WHERE interactive_id = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.feedback_by_interactive (
    interactive_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    feedback_ids list<timeuuid>,
    PRIMARY KEY (interactive_id, deployment_id, change_id)
)
WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
