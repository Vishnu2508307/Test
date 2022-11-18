--
-- iam_global.account_by_historical_id
-- -----------------------------------
-- Track a user account by using the historical (mysql) id.
--
-- Find an account with the supplied the historical id
-- > SELECT * FROM iam_global.account_by_historical_id WHERE historical_id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.account_by_historical_id (
    historical_id bigint,
    account_id timeuuid,
    iam_region text,
    PRIMARY KEY (historical_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
