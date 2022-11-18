--
-- learner_insight.progress_stats_by_deployment
-- --------------------------------------------
-- This table stores aggregated stats about all the learners progress for a given deployment
--
-- Fetch all the aggregate stats for a given deployment and element
-- > SELECT * FROM learner_insight.progress_stats_by_deployment WHERE deployment_id=? and courseware_element_id=?;
--
-- Fetch one stat type for a given deployment and element
-- > SELECT * FROM learner_insight.progress_stats_by_deployment WHERE deployment_id=? and courseware_element_id=? and stat_type=?;

CREATE TABLE IF NOT EXISTS learner_insight.progress_stats_by_deployment (
    deployment_id timeuuid,
    courseware_element_id timeuuid,
    courseware_element_type text,
    stat_type text,
    stat_value float,
    PRIMARY KEY ((deployment_id, courseware_element_id, stat_type))
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
