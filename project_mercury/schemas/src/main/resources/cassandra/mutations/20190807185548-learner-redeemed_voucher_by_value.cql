--
-- learner.redeemed_voucher_by_value
-- --------------------
-- Track redeemed voucher by value
--
-- Find a redeemed voucher information by value
-- > SELECT * FROM learner.redeemed_voucher_by_value WHERE voucher_value = ?
--
CREATE TABLE IF NOT EXISTS learner.redeemed_voucher_by_value (
    voucher_value text,
    id timeuuid,
    redeemed_for_cohort timeuuid,
    redeemed_by_account timeuuid,
    PRIMARY KEY (voucher_value)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
