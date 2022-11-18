--
-- courseware.scenario_by_interactive
-- -------------------------------
-- Track all the scenarios by interactive
--
-- Find all the scenarios on a interactive
-- > SELECT * FROM courseware.scenario_by_interactive WHERE interactive_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.scenario_by_interactive (
    interactive_id timeuuid,
    scenario_id timeuuid,
    PRIMARY KEY(interactive_id, scenario_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
