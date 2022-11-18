--
-- iam_global.fidm_ltiv11_launch_request
-- -------------------------------------
-- Track the headers and LTI parameters used in the LTI launch requests
--
-- Find all the headers and LTI parameters for a launch request
-- > SELECT * FROM iam_global.fidm_ltiv11_launch_request WHERE id = ?
--
-- Find a LTI parameter from a launch request
-- > SELECT * FROM iam_global.fidm_ltiv11_launch_request WHERE id = ? AND part = 'PARAM' AND name = ?
--
CREATE TABLE IF NOT EXISTS iam_global.fidm_ltiv11_launch_request (
    id timeuuid,
    request_url text static,
    ltiv11_credential_id timeuuid static,
    part text,
    name text,
    value text,
    PRIMARY KEY(id, part, name)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
