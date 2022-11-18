--
-- publication.summary
-- --------------------
-- This table store the publication summary details.
--
-- Find the publication summary details for given id
-- > SELECT * FROM publication.summary WHERE id=?
--
CREATE TABLE IF NOT EXISTS publication.summary (
    id timeuuid,
    title text,
    description text,
    config text,
    PRIMARY KEY (id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
