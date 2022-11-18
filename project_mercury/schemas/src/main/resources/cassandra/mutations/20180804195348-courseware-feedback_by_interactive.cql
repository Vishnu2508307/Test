--
-- courseware.feedback_by_interactive
-- ----------------------------------
-- Track all feedback associated to a particular interactive
--
-- Find all the feedback for an interactive
-- > SELECT * FROM courseware.feedback_by_interactive WHERE interactive_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.feedback_by_interactive (
    interactive_id timeuuid,
    feedback_id timeuuid,
    PRIMARY KEY (interactive_id, feedback_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
