--
-- iam_global.fidm_oidc_session_log
-- --------------------------------
-- Track OIDC Session events, ordered by the newest events at the top
--
-- Find a session log by session id
-- > SELECT * FROM iam_global.fidm_oidc_session_log WHERE session_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_oidc_session_log (
    session_id text,
    id timeuuid,
    action text,
    message text,
    PRIMARY KEY(session_id, id)
)
WITH CLUSTERING ORDER BY (id DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
