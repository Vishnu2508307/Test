--
-- courseware.scenario_by_parent
-- --------------------
-- Keeps the relationship between a scenario and its parent entity
--
-- Find all the scenarios by parent_id and lifecycle
-- > SELECT * FROM courseware.scenario_by_parent WHERE parent_id = ? AND lifecycle = ?
--
CREATE TABLE IF NOT EXISTS courseware.scenario_by_parent (
    parent_id timeuuid,
    lifecycle text,
    scenario_ids list<timeuuid>,
    PRIMARY KEY (parent_id, lifecycle)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
