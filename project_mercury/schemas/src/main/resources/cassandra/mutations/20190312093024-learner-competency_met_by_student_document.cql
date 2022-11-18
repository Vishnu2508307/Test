--
-- learner.competency_met_by_student_document
-- ------------------------------------------
-- Stores the competencies met by student and document. It stores only the latest achivements.
--
-- Fetch all competencies met for a learner by student id
-- > SELECT * FROM learner.competency_met_by_student_document WHERE student_id = ?;
-- Fetch all competencies met for a learner by student and document
-- > SELECT * FROM learner.competency_met_by_student_document WHERE student_id = ? and document_id = ?;
-- Fetch all competencies met for a learner by student and document_item
-- > SELECT * FROM learner.competency_met_by_student_document WHERE student_id = ? and document_id = ? and document_item_id = ?;
--
CREATE TABLE IF NOT EXISTS learner.competency_met_by_student_document (
    student_id timeuuid,
    document_id uuid,
    document_item_id uuid,
    met_id timeuuid,
    value float,
    confidence float,
    PRIMARY KEY (student_id, document_id, document_item_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
