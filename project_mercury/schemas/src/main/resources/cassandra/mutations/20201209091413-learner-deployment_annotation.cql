--
-- learner.deployment_annotation
-- -----------------------------
-- Store annotation created in the workspace and published along with a deployment
--
-- Find all the published annotation for a deployment by motivation and element id
-- > SELECT * FROM learner.deployment_annotation WHERE deployment_id = ?
-- AND change_id = ?
-- AND motivation = ?
-- AND element_id = ?
--
CREATE TABLE IF NOT EXISTS learner.deployment_annotation (
    deployment_id timeuuid,
    change_id timeuuid,
    motivation text,
    element_id timeuuid,
    annotation_id timeuuid,
    creator_account_id timeuuid,
    PRIMARY KEY ((deployment_id, change_id), motivation, element_id, annotation_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};


