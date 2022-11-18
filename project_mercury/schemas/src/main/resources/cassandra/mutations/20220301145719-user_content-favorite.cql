--
-- user_content.favorite
-- --------------------
-- favorite table to track the users favorite courses/documents/workspaces
--
-- Write a description of the query that this table supports
-- > SELECT * FROM user_content.favorite WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS user_content.favorite (
                                 id timeuuid,
                                 account_id timeuuid,
                                 root_element_id timeuuid,
                                 workspace_id timeuuid,
                                 project_id timeuuid,
                                 activity_id timeuuid,
                                 document_id timeuuid,
                                 resource_type text,
                                 created_at timeuuid,
                                 PRIMARY KEY (id, account_id)
)
    WITH compression = {
        'sstable_compression' : 'SnappyCompressor'
        };
