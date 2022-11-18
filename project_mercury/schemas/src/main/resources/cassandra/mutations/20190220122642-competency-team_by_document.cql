--
-- competency.team_by_document
-- ---------------------
-- Stores all the teams collaborating on a document.
--
-- Find all teams that are collaborating on a document
-- > SELECT * FROM competency.team_by_document WHERE document_id = ?;
--
CREATE TABLE IF NOT EXISTS competency.team_by_document (
    document_id uuid,
    team_id timeuuid,
    permission_level text,
    PRIMARY KEY(document_id, team_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
