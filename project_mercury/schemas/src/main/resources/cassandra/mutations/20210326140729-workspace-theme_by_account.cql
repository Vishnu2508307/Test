--
-- workspace.theme_by_account
-- --------------------
-- Stores the themes for a given account
--
-- Get list of themes for a given account_id
-- > SELECT * FROM workspace.theme_by_account WHERE account_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.theme_by_account (
    account_id timeuuid,
    theme_id timeuuid,
    PRIMARY KEY(account_id, theme_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
