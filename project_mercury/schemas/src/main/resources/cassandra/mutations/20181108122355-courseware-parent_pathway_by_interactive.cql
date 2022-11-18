--
-- courseware.parent_pathway_by_interactive
-- -----------------------------------------
-- Tracks parent pathways for interactives
--
-- Find a parent pathway the interactive is attached to
-- > SELECT * FROM courseware.parent_pathway_by_interactive WHERE interactive_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.parent_pathway_by_interactive (
    interactive_id timeuuid,
    pathway_id timeuuid,
    PRIMARY KEY (interactive_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
