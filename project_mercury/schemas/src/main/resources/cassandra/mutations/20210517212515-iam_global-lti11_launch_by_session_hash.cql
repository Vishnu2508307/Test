--
-- iam_global.lti11_launch_by_session_hash
-- ---------------------------------------
-- Track a session.js init by hash and launch request id
--
-- Find the session.js init by hash and launch_request_id
-- > SELECT * FROM iam_global.lti11_launch_by_session_hash WHERE hash = ? AND launch_request_id = ?
--
CREATE TABLE IF NOT EXISTS iam_global.lti11_launch_by_session_hash (
    hash text,
    launch_request_id timeuuid,
    user_id text,
    cohort_id timeuuid,
    configuration_id timeuuid,
    continue_to text,
    status text,
    PRIMARY KEY (hash, launch_request_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
