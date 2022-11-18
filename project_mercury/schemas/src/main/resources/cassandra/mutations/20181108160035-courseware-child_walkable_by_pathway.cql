--
-- courseware.child_walkable_by_pathway
-- -------------------------------------
-- Store the relationship between a pathway and its walkable children
--
-- Find walkable children by pathway
-- > SELECT * FROM courseware.child_walkable_by_pathway WHERE pathway_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.child_walkable_by_pathway (
    pathway_id timeuuid,
    walkable_ids list<timeuuid>,
    walkable_types map<timeuuid, text>,
    PRIMARY KEY (pathway_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
