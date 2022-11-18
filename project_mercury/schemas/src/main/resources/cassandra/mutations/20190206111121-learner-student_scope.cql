--
-- learner.student_scope
-- --------------------
-- Tracks all student scopes by deployment, account and scope URN
--
-- Fetch the latest scope id for deployment, account and URN
-- > SELECT * FROM learner.student_scope WHERE deployment_id = ? and account_id = ? and scope_urn = ?
--
CREATE TABLE IF NOT EXISTS learner.student_scope (
    id timeuuid,
    deployment_id timeuuid,
    account_id timeuuid,
    scope_urn timeuuid,
    PRIMARY KEY ((deployment_id, account_id, scope_urn), id)
)
WITH CLUSTERING ORDER BY (id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};