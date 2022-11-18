--
-- learner_redirect.by_key
-- -----------------------
-- Track redirects by type and key
--
-- Find a redirect by type and key
-- > SELECT * FROM learner_redirect.by_key WHERE redirect_type = ? AND redirect_key = ?;
--
CREATE TABLE IF NOT EXISTS learner_redirect.by_key (
    redirect_type text,
    redirect_key text,
    id timeuuid,
    version timeuuid,
    destination_path text,
    PRIMARY KEY ((redirect_type, redirect_key))
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
