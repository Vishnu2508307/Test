--
-- learner.evaluation_error_by_type
-- --------------------------------
-- Tracks errors occurring during evaluation of an interactive
--
-- Find all errors of a specific type
-- > SELECT * FROM learner.evaluation_error_by_type WHERE type = ?
--
CREATE TABLE IF NOT EXISTS learner.evaluation_error_by_type (
    type text,
    evaluation_id timeuuid,
    id timeuuid,
    occurred_at text,
    error text,
    stacktrace text,
    PRIMARY KEY ((type), id, evaluation_id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
