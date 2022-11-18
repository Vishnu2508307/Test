--
-- learner.grade_passback_notification
-- --------------------
-- Stores grade passback notifications
--
-- Find a notification by id
-- > SELECT * FROM learner.grade_passback_notification WHERE notification_id = ?
--
CREATE TABLE IF NOT EXISTS learner.grade_passback_notification (
    notification_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    student_id timeuuid,
    result_score double,
    status text,
    completed_at timeuuid,
    PRIMARY KEY (notification_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
