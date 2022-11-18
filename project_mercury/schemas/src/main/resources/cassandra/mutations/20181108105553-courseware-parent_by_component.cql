--
-- courseware.parent_by_component
-- --------------------------------
-- Keeps a parent entity for a component
--
-- Find a parent entity for the component
-- > SELECT * FROM courseware.parent_by_component WHERE component_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.parent_by_component (
    component_id timeuuid,
    parent_id timeuuid,
    parent_type text,
    PRIMARY KEY (component_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
