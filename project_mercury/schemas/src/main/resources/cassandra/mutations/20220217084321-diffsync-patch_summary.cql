--
-- diffsync.patch_summary
-- --------------------
-- Stores patches information of differential synchronization
--
-- Fetch a patch by entity id and name
-- > SELECT * FROM diffsync.patch_summary WHERE entityId = ? and entityName = ?
--
CREATE TABLE IF NOT EXISTS diffsync.patch_summary (
    id timeuuid,
    client_id text,
    entity_id timeuuid,
    entity_name text,
    patches  text,
    n_version bigint,
    m_version bigint,
    PRIMARY KEY (entity_id, entity_name)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
