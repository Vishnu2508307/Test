--
-- courseware.annotation
-- ---------------------
-- Persist annotation by id
--
-- Find an annotation by id
-- > SELECT * FROM courseware.annotation WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.annotation (
    id timeuuid,
    version timeuuid,
    annotation_type text,
    motivation text,
    creator_account_id timeuuid,
    body text,
    target text,
    root_element_id timeuuid,
    element_id timeuuid,
    PRIMARY KEY (id, version)
)
WITH CLUSTERING ORDER BY (version DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
