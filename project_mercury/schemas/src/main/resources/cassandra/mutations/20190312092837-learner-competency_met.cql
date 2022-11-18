--
-- learner.competency_met
-- ----------------------
-- Stores the competency met for a learner. This is immutable. If a student reachived something, the new record will be created.
--
-- Select a summary of the competency met for a learner by competency met id
-- > SELECT * FROM learner.competency_met WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS learner.competency_met (
    id timeuuid,
    student_id timeuuid,
    deployment_id timeuuid,
    change_id timeuuid,
    element_id timeuuid,
    element_type text,
    evaluation_id timeuuid,
    document_id uuid,
    document_version_id timeuuid,
    item_id uuid,
    attempt_id timeuuid,
    value float,
    confidence float,
    PRIMARY KEY(id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
