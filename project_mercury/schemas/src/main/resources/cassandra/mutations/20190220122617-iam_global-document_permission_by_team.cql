--
-- iam_global.document_permission_by_team
-- --------------------------------------
-- Store document permissions by team.
--
-- Find all permissions for a team
-- > SELECT * FROM iam_global.document_permission_by_team WHERE team_id = ?;
--
-- Find team permissions for a document
-- > SELECT * FROM iam_global.document_permission_by_team WHERE team_id = ? and document_id = ?;
--
CREATE TABLE IF NOT EXISTS iam_global.document_permission_by_team (
    team_id timeuuid,
    document_id uuid,
    permission_level text,
    PRIMARY KEY(team_id, document_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
