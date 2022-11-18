--
-- learner.competency_met_by_student_document_history
-- ------------------------------------------
-- Stores the history of all competencies met by student and document.
--
-- Fetch all competencies met for a learner by student id
-- > SELECT * FROM learner.competency_met_by_student_document_history WHERE student_id = ?;
-- Fetch all competencies met for a learner by student and document
-- > SELECT * FROM learner.competency_met_by_student_document_history WHERE student_id = ? and document_id = ?;
-- Fetch all competencies met for a learner by student and document_item
-- > SELECT * FROM learner.competency_met_by_student_document_history WHERE student_id = ? and document_id = ? and document_item_id = ?;
--
CREATE TABLE IF NOT EXISTS learner.competency_met_by_student_document_history (
    student_id timeuuid,
    document_id uuid,
    document_item_id uuid,
    met_id timeuuid,
    value float,
    confidence float,
    PRIMARY KEY (student_id, document_id, document_item_id, met_id)
)
WITH CLUSTERING ORDER BY (document_id ASC, document_item_id ASC, met_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
