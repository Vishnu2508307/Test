--
-- user_content.shared_resource
-- --------------------
-- shared_resource to track shared courses between users in user content
--
--
-- > SELECT * FROM user_content.shared_resource WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS user_content.shared_resource (
        id timeuuid,
        account_id timeuuid,
        shared_account_id timeuuid,
        resource_id timeuuid,
        resource_type text,
        shared_at timeuuid,
        PRIMARY KEY (account_id, shared_account_id, id)
    )
    WITH compression = {
        'sstable_compression' : 'SnappyCompressor'
        };