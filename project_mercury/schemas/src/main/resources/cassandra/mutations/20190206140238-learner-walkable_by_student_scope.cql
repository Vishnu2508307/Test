--
-- learner.walkable_by_student_scope
-- --------------------
-- Allows to find the element the student_scope_urn belongs to in a learner context
--
-- Find the element the student scope urn belongs to
-- > SELECT * FROM learner.walkable_by_student_scope WHERE student_scope_urn = ? AND deployment_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.walkable_by_student_scope (
    student_scope_urn timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    walkable_id timeuuid,
    walkable_type text,
    PRIMARY KEY (student_scope_urn, deployment_id, change_id, walkable_id)
) WITH CLUSTERING ORDER BY (deployment_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
