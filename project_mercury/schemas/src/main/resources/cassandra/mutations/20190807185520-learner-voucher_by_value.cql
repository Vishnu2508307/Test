--
-- learner.voucher_by_value
-- --------------------
-- Track each voucher value ever issued by value
--
-- Find voucher information by voucher value
-- > SELECT * FROM learner.voucher_by_value WHERE voucher_value = ?
--
CREATE TABLE IF NOT EXISTS learner.voucher_by_value (
    voucher_value text,
    id timeuuid,
    state text,
    generated_by_account timeuuid,
    ttl_seconds int,
    created_at text,
    expires_at text,
    comment text,
    PRIMARY KEY (voucher_value)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
