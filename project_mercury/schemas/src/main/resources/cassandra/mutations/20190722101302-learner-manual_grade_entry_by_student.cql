--
-- learner.manual_grade_entry_by_student
-- -------------------------------------
-- Tracks the manual grades entries for a student on a particular component
--
-- Find all the student manual grades over a component attempt
-- > SELECT * FROM learner.manual_grade_entry_by_student
-- WHERE deployment_id = ?
-- AND student_id = ?
-- AND component_id = ?
-- AND attempt_id = ?
--
CREATE TABLE IF NOT EXISTS learner.manual_grade_entry_by_student (
    deployment_id timeuuid,
    student_id timeuuid,
    component_id timeuuid,
    attempt_id timeuuid,
    id timeuuid,
    max_score double,
    score double,
    change_id timeuuid,
    parent_id timeuuid,
    parent_type text,
    operator text,
    instructor_id timeuuid,
    PRIMARY KEY ((deployment_id, student_id, component_id, attempt_id), id)

) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
