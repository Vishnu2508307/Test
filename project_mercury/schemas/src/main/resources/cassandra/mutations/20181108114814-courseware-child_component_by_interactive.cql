--
-- courseware.child_component_by_interactive
-- ------------------------------------------
-- Tracks components added to interactive
--
-- Find all the component ids by interactive
-- > SELECT * FROM courseware.child_component_by_interactive WHERE interactive_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.child_component_by_interactive (
    interactive_id timeuuid,
    component_id timeuuid,
    PRIMARY KEY (interactive_id, component_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
