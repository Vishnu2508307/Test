--
-- learner.redeemed_voucher
-- --------------------
-- Track all redeemed voucher. Allows to query for date ranges
--
-- Find redeemed voucher by id
-- > SELECT * FROM learner.redeemed_voucher WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.redeemed_voucher (
    id timeuuid,
    voucher_value text,
    redeemed_for_cohort timeuuid,
    redeemed_by_account timeuuid,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
