--
-- learner.student_scope_entry_by_source
-- --------------------
-- This table keeps student scopes entires
--
-- Fetch the latest student scope data for the given scope id and source id
-- > SELECT * FROM learner.student_scope_entry_by_source WHERE scope_id = ? and source_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.student_scope_entry_by_source (
    scope_id timeuuid,
    source_id timeuuid,
    id timeuuid,
    data text,
    PRIMARY KEY (scope_id, source_id, id)
)
WITH CLUSTERING ORDER BY (source_id ASC, id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
