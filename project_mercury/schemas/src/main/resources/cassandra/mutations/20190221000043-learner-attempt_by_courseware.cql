--
-- learner.attempt_by_courseware
-- -----------------------------
-- Track attempts of a learner newest first, keyed by deployment, courseware element and student id
--
-- Find the latest attempt by courseware
-- > SELECT * FROM learner.attempt_by_courseware WHERE deployment_id = ? AND courseware_element_id = ? AND student_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.attempt_by_courseware (
    id timeuuid,
    parent_id timeuuid,
    deployment_id timeuuid,
    courseware_element_id timeuuid,
    courseware_element_type text,
    student_id timeuuid,
    value int,
    PRIMARY KEY ((deployment_id, courseware_element_id, student_id), id)
)
WITH CLUSTERING ORDER BY (id DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
