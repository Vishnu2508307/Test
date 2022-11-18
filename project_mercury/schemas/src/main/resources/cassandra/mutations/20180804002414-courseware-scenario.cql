--
-- courseware.scenario
-- -------------------
-- Track created scenarios
--
-- Find a scenario by id
-- > SELECT * FROM courseware.scenario WHERE id = ?
--
CREATE TABLE IF NOT EXISTS courseware.scenario (
    id timeuuid,
    interactive_id timeuuid,
    condition text,
    action text,
    PRIMARY KEY (id)
) WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
