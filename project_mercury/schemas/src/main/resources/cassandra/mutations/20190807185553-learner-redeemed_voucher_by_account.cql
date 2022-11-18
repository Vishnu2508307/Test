--
-- learner.redeemed_voucher_by_account
-- --------------------
-- Track all redeemed voucher by an account
--
-- Find all the voucher redeemed by an account
-- > SELECT * FROM learner.redeemed_voucher_by_account WHERE redeemed_by_account = ?
--
CREATE TABLE IF NOT EXISTS learner.redeemed_voucher_by_account (
    redeemed_by_account timeuuid,
    voucher_value text,
    id timeuuid,
    redeemed_for_cohort timeuuid,
    PRIMARY KEY (redeemed_by_account, voucher_value)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
