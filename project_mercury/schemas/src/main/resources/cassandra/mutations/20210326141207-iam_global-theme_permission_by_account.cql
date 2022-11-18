--
-- iam_global.theme_permission_by_account
-- --------------------
-- Tracks permissions for themes
--
-- Find a permission level for an account for a theme
-- > SELECT * FROM iam_global.theme_permission_by_account WHERE account_id = ? and theme_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.theme_permission_by_account (
    account_id timeuuid,
    theme_id timeuuid,
    permission_level text,
    PRIMARY KEY(account_id, theme_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
