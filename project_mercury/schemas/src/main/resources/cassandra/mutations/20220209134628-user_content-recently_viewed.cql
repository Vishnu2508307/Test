--
-- user_content.recently_viewed
-- --------------------
-- recently_viewed table to track users recently viewd project/course/document
--
-- Write a description of the query that this table supports
-- > SELECT * FROM user_content.recently_viewed WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS user_content.recently_viewed (
        account_id timeuuid,
        root_element_id timeuuid,
        workspace_id timeuuid,
        project_id timeuuid,
        activity_id timeuuid,
        document_id timeuuid,
        resource_type text,
        last_viewed_at timeuuid,
        PRIMARY KEY (account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
