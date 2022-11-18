--
-- workspace.summary
-- -----------------
-- This table stores the summary workspace
--
-- Select a workspace by id
-- > SELECT * FROM workspace.summary WHERE id=?
--
CREATE TABLE IF NOT EXISTS workspace.summary (
    id timeuuid,
    name text,
    description text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
