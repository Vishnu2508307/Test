--
-- iam_global.subscription
-- -----------------------
-- Associates multiple accounts to the same set of shared configuration and limits.
--
-- Find the subscription by id
-- > SELECT * FROM iam_global.subscription WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.subscription (
    id timeuuid,
    name text,
    iam_region text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
