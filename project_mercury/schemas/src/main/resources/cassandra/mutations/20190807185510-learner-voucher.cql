--
-- learner.voucher
-- --------------------
-- Track all access voucher ever issued. Allows to query for date ranges
--
-- Find voucher by id
-- > SELECT * FROM learner.voucher WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.voucher (
    id timeuuid,
    voucher_value text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
