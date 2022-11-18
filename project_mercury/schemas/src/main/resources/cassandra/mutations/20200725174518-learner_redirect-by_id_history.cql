--
-- learner_redirect.by_id_history
-- ------------------------------
-- Track the history of the redirects, by latest to oldest
--
-- Find all the redirects over time by_id_history id
-- > SELECT * FROM learner_redirect.by_id_history WHERE id = ?;
--
-- Find a specific version of a redirect by id
-- > SELECT * FROM learner_redirect.by_id_history WHERE id = ? AND version = ?;
--
CREATE TABLE IF NOT EXISTS learner_redirect.by_id_history (
    id timeuuid,
    version timeuuid,
    redirect_type text,
    redirect_key text,
    destination_path text,
    PRIMARY KEY (id, version)
)
WITH CLUSTERING ORDER BY (version DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
