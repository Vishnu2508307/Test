--
-- courseware.component
-- --------------------
-- Store information about a component
--
-- Find a component by id
-- > SELECT * FROM courseware.component WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.component (
    id timeuuid,
    interactive_id timeuuid,
    plugin_id timeuuid,
    plugin_version_expr text,
    PRIMARY KEY(id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
