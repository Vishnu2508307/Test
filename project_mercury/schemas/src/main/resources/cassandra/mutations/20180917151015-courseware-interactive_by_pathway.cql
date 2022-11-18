--
-- courseware.interactive_by_pathway
-- --------------------
-- Track interactives on a pathway
--
-- Find all the Interactives for a Pathway
-- > SELECT * FROM courseware.interactive_by_pathway WHERE pathway_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.interactive_by_pathway (
    pathway_id timeuuid,
    interactive_id timeuuid,
    PRIMARY KEY (pathway_id, interactive_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
