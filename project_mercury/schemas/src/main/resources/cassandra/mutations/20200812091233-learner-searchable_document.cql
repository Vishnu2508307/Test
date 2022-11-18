--
-- learner.searchable_by_deployment
-- --------------------------------
-- This table is designed to store the searchable values.
-- Using these stored values to generate CSG messages
--
-- Find latest searchable field by given deployment_id, element_id and searchable_field
-- > SELECT * FROM learner.searchable_by_deployment WHERE deployment_id = ? and element_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS learner.searchable_document (
    deployment_id timeuuid,
    element_id timeuuid,
    searchable_field_id timeuuid,
    change_id timeuuid,
    product_id text,
    cohort_id timeuuid,
    element_type text,
    element_path list<timeuuid>,
    content_type text,
    summary text,
    body text,
    source text,
    preview text,
    tag text,
    PRIMARY KEY(deployment_id, element_id, searchable_field_id, change_id)
) WITH CLUSTERING ORDER BY (element_id ASC, searchable_field_id ASC, change_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};