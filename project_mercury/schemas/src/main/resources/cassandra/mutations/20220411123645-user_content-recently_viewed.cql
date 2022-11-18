--
-- user_content.recently_viewed
-- --------------------
-- recently_viewed to track user recently viewed content
--
-- Description of the query that this table supports
-- > SELECT * FROM user_content.recently_viewed WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS user_content.recently_viewed (
    id timeuuid,
    account_id timeuuid,
    root_element_id timeuuid,
    workspace_id timeuuid,
    project_id timeuuid,
    activity_id timeuuid,
    document_id timeuuid,
    resource_type text,
    last_viewed_at timeuuid,
    PRIMARY KEY (account_id, id)
)
    WITH compression = {
        'sstable_compression' : 'SnappyCompressor'
        };