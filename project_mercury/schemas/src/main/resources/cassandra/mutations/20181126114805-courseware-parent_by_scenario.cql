--
-- courseware.parent_by_scenario
-- --------------------
-- Store the upward relationship between a scenario and its parent.
--
-- Find the parent element of a given scenario
-- > SELECT * FROM courseware.parent_by_scenario WHERE scenario_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.parent_by_scenario (
    scenario_id timeuuid,
    parent_id timeuuid,
    parent_type text,
    PRIMARY KEY (scenario_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
