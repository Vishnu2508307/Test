--
-- courseware.annotation_by_motivation
-- -----------------------------------
-- Track courseware annotations for a root element, filterable by element id, motivation
--
-- Find annotations on a root element
-- > SELECT * FROM courseware.annotation_by_motivation WHERE root_element_id = ?
--
-- Find annotations on a root element by a specific motivation
-- > SELECT * FROM courseware.annotation_by_motivation WHERE root_element_id = ? AND motivation = ?
--
-- Find annotations on a root element by a specific motivation and element
-- > SELECT * FROM courseware.annotation_by_motivation WHERE root_element_id = ? AND motivation = ? AND element_id = ?
--
CREATE TABLE IF NOT EXISTS courseware.annotation_by_motivation (
    root_element_id timeuuid,
    element_id timeuuid,
    motivation text,
    annotation_id timeuuid,
    PRIMARY KEY((root_element_id), motivation, element_id, annotation_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
