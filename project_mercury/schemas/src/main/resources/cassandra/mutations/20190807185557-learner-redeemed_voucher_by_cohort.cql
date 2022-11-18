--
-- learner.redeemed_voucher_by_cohort
-- --------------------
-- Track all the voucher that have been redeemed for a cohort
--
-- Find all the voucher that have been redeemed for a cohort
-- > SELECT * FROM learner.redeemed_voucher_by_cohort WHERE cohort_id = ?
--
CREATE TABLE IF NOT EXISTS learner.redeemed_voucher_by_cohort (
    redeemed_for_cohort timeuuid,
    voucher_value text,
    id timeuuid,
    redeemed_by_account timeuuid,
    PRIMARY KEY (redeemed_for_cohort, voucher_value)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
