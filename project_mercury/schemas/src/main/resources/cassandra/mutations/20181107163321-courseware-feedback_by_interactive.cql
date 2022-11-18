--
-- courseware.feedback_by_interactive
-- ----------------------------------
-- Track all feedback associated to a particular interactive. Requires ordering
--
-- Find all the feedback for an interactive
-- > SELECT * FROM courseware.feedback_by_interactive WHERE interactive_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.feedback_by_interactive (
    interactive_id timeuuid,
    feedback_ids list<timeuuid>,
    PRIMARY KEY (interactive_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
