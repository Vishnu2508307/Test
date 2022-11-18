--
-- cohort.cohort_by_product
-- --------------------
-- Associates cohort (or cohort template for classes on-demand) by product id
--
-- Find the cohort associated to a product id
-- > SELECT cohort_id FROM cohort.cohort_by_product WHERE product_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.cohort_by_product (
    product_id text,
    cohort_id timeuuid,
    PRIMARY KEY (product_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
