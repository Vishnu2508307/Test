--
-- competency.document_by_team
-- ---------------------
-- Stores the documents that a team has access to.
--
-- Find all documents that a team has access to.
-- > SELECT * FROM competency.document_by_team WHERE team_id = ?;
--
CREATE TABLE IF NOT EXISTS competency.document_by_team (
    team_id timeuuid,
    document_id uuid,
    PRIMARY KEY(team_id,document_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
