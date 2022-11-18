--
-- iam_global.credential_temporary_by_account
-- ------------------------------------------
-- Find all the credentials by account. This should only be used as an index after a successful operation to remove
-- other temporary credentials of the same type on an account.
--
-- Find all the temporary credentials for an account
-- > SELECT * FROM iam_global.credential_temporary_by_account WHERE account_id = ?;
--
-- Find all the temporary credentials for an account and type
-- > SELECT * FROM iam_global.credential_temporary_by_account WHERE account_id = ? AND type = ?;
--
-- Find all the temporary credentials for an account and type and specific code
-- > SELECT * FROM iam_global.credential_temporary_by_account WHERE account_id = ? AND type = ? AND authorization_code = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.credential_temporary_by_account (
    account_id timeuuid,
    authorization_code text,
    type text,
    PRIMARY KEY (account_id, type, authorization_code)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
