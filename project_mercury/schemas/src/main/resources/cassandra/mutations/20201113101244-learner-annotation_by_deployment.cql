--
-- learner.annotation_by_deployment
-- --------------------
-- Track learner annotations for a deployment, filterable by motivation, element id
--
-- Find annotations on a deployment
-- > SELECT * FROM learner.annotation_by_motivation WHERE deployment_id = ?;
--
-- Find annotations on a deployment for a specific motivation
-- > SELECT * FROM learner.annotation_by_motivation WHERE deployment_id = ? AND motivation = ?;
--
-- Find annotations on a deployment for a specific motivation and element
-- > SELECT * FROM learner.annotation_by_motivation WHERE deployment_id = ? AND motivation = ? AND element_id = ?;
--
CREATE TABLE IF NOT EXISTS learner.annotation_by_deployment (
    deployment_id timeuuid,
    creator_account_id timeuuid,
    motivation text,
    element_id timeuuid,
    annotation_id timeuuid,
    PRIMARY KEY((deployment_id), motivation, element_id, annotation_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
