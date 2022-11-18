--
-- courseware.parent_interactive_by_feedback
-- --------------------------------------------
-- Keeps a parent interactive for feedback
--
-- Find an interactive which the feddback belongs to
-- > SELECT * FROM courseware.parent_interactive_by_feedback WHERE feedback_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.parent_interactive_by_feedback (
    feedback_id timeuuid,
    interactive_id timeuuid,
    PRIMARY KEY (feedback_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};