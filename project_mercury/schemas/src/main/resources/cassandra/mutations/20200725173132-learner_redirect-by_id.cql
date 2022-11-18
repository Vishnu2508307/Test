--
-- learner_redirect.by_id
-- ----------------------
-- Track learner redirects by id
--
-- Find a learner redirect by id
-- > SELECT * FROM learner_redirect.by_id WHERE id = ?;
--
CREATE TABLE IF NOT EXISTS learner_redirect.by_id (
    id timeuuid,
    version timeuuid,
    redirect_type text,
    redirect_key text,
    destination_path text,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
