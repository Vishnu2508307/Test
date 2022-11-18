--
-- learner.evaluation_error_by_evaluation_id
-- ------------------------------------------
-- Tracks errors occurring during evaluation of an interactive
--
-- Find all the errors that occurred during a particular evaluation
-- > SELECT * FROM learner.evaluation_error_by_evaluation_id WHERE evaluation_id = ?
--
CREATE TABLE IF NOT EXISTS learner.evaluation_error_by_evaluation_id (
    evaluation_id timeuuid,
    type text,
    id timeuuid,
    occurred_at text,
    error text,
    stacktrace text,
    PRIMARY KEY ((evaluation_id), type, id)
) WITH CLUSTERING ORDER BY (type DESC, id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
