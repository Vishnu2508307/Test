--
-- learner.latest_student_scope_entry_by_source
-- --------------------------------------------
-- Store the latest scope entry for written by a source_id. This table is used to query the latest data when
-- creating a snapshot.
--
-- Find all scope entries for a scope_id written by source_ids
-- > SELECT * FROM learner.latest_student_scope_entry_by_source WHERE scope_id = ?
--
CREATE TABLE IF NOT EXISTS learner.latest_student_scope_entry_by_source (
    scope_id timeuuid,
    source_id timeuuid,
    data text,
    PRIMARY KEY (scope_id, source_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
