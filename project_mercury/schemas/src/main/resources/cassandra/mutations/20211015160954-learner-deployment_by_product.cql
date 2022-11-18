--
-- learner.deployment_by_product
-- --------------------
-- Tracks the deployment tied to a product
--
-- Find the deployment tied to a product
-- > SELECT * FROM learner.deployment_by_product WHERE product_id = ?
--
CREATE TABLE IF NOT EXISTS learner.deployment_by_product (
    product_id text,
    deployment_id timeuuid,
    PRIMARY KEY (product_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
