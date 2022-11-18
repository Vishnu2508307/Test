--
-- learner.grade_passback_notification_by_deployment
-- --------------------
-- Used to track grade passback notifications by deployment
--
-- Find the latest grade passback notifications by deployment and student
-- > SELECT * FROM learner.grade_passback_notification_by_deployment WHERE deployment_id = ? AND student_id = ?
--
CREATE TABLE IF NOT EXISTS learner.grade_passback_notification_by_deployment (
    deployment_id timeuuid,
    change_id timeuuid,
    student_id timeuuid,
    notification_id timeuuid,
    result_score double,
    status text,
    completed_at timeuuid,
    PRIMARY KEY ((deployment_id, change_id, student_id), notification_id)
)
WITH CLUSTERING ORDER BY (notification_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
