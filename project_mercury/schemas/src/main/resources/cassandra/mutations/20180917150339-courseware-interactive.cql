--
-- courseware.interactive
-- --------------------
-- Store Interactive objects
--
-- Find an Interactive by id
-- > SELECT * FROM courseware.interactive WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.interactive (
    id timeuuid,
    pathway_id timeuuid,
    plugin_id timeuuid,
    plugin_version_expr text,
    PRIMARY KEY(id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
}
;
