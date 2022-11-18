--
-- learner.available_voucher
-- --------------------
-- Track all the available voucher values in the pool
--
-- Find if an voucher value is available
-- > SELECT * FROM learner.available_voucher WHERE voucher_value = ?
--
CREATE TABLE IF NOT EXISTS learner.available_voucher (
    voucher_value text,
    PRIMARY KEY (voucher_value)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
