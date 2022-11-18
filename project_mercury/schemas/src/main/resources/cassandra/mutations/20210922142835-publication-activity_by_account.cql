--
-- publication.activity_by_account
-- --------------------
-- This table store the activity by account.
--
-- Find the activity for given account id
-- > SELECT * FROM publication.activity_by_account WHERE account_id=?
--
CREATE TABLE IF NOT EXISTS publication.activity_by_account (
    account_id timeuuid,
    activity_id timeuuid,
    PRIMARY KEY (account_id, activity_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
