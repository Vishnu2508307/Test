--
-- courseware.walkable_by_student_scope
-- --------------------
-- Allows to find a walkable element by student_scope_urn.
--
-- Find a walkable by student_scope_urn
-- > SELECT * FROM courseware.walkable_by_student_scope WHERE student_scope_urn = ?
--
CREATE TABLE IF NOT EXISTS courseware.walkable_by_student_scope (
    student_scope_urn timeuuid,
    walkable_id timeuuid,
    walkable_type text,
    PRIMARY KEY (student_scope_urn)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
