--
-- cohort.instance_by_template
-- --------------------
-- Tracks the instance cohort ids using the template cohort id
--
-- Find all the instance cohort ids by template cohort id
-- > SELECT * FROM cohort.instance_by_template WHERE template_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.instance_by_template (
    template_id timeuuid,
    instance_id timeuuid,
    PRIMARY KEY (template_id, instance_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
