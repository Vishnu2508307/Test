--
-- courseware.asset_by_root_activity
-- --------------------
-- Store asset information by root element
--
--
-- Find an asset by provider and root element
-- SELECT * FROM <keyspace>.<table_name> WHERE root_element_id = ? AND asset_provider = 'alfresco';
-- Find all assets by root element
-- SELECT * FROM <keyspace>.<table_name> WHERE root_element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.asset_by_root_activity (
     root_element_id timeuuid,
     element_id timeuuid,
     element_type text,
     asset_provider text,
     asset_id timeuuid,
     PRIMARY KEY (root_element_id, asset_provider, asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
