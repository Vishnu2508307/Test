--
-- workspace.account_by_theme
-- --------------------
-- Stores the list accounts for a given theme
--
-- Get list of accounts for a given theme_id
-- > SELECT * FROM workspace.account_by_theme WHERE theme_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.account_by_theme (
    theme_id timeuuid,
    account_id timeuuid,
    permission_level text,
    PRIMARY KEY(theme_id, account_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
