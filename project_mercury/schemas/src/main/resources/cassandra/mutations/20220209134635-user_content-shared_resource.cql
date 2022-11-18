--
-- user_content.shared_resource
-- --------------------
-- shared_resource to track shared courses between users
--
-- Write a description of the query that this table supports
-- > SELECT * FROM user_content.shared_resource WHERE account_id = ? and shared_account_id = ?
--
CREATE TABLE IF NOT EXISTS user_content.shared_resource (
        account_id timeuuid,
        shared_account_id timeuuid,
        resource_id timeuuid,
        resource_type text,
        shared_at timeuuid,
        PRIMARY KEY (account_id, shared_account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
