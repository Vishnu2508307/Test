--
-- workspace.theme
-- --------------------
-- This table is designed to record the information about theme.
--
-- Find theme information by id
-- > SELECT * FROM workspace.theme WHERE id =?
--
CREATE TABLE IF NOT EXISTS workspace.theme (
    id timeuuid,
    name text,
    config text,
    primary key (id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
