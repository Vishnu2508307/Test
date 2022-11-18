--
-- user_content.favorite
-- --------------------
-- favorite table to track the users favorite courses/documents/workspaces
--
-- Write a description of the query that this table supports
-- > SELECT * FROM user_content.favorite WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS user_content.favorite (
     account_id timeuuid,
     root_element_id timeuuid,
     workspace_id timeuuid,
     project_id timeuuid,
     activity_id timeuuid,
     document_id timeuuid,
     resource_type text,
     created_at timeuuid,
     PRIMARY KEY (account_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
