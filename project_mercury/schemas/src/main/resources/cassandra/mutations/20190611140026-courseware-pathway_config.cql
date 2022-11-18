--
-- courseware.pathway_config
-- --------------------
-- Holds the optional pathway configuration object. All versions are stored and ordered by latest
--
-- Find the latest config for a pathway
-- > SELECT * FROM courseware.pathway_config WHERE pathway_id = ? LIMIT 1
--
CREATE TABLE IF NOT EXISTS courseware.pathway_config (
    pathway_id timeuuid,
    id timeuuid,
    config text,
    PRIMARY KEY (pathway_id, id)
) WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
