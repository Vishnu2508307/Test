--
-- learner.attempt
-- ---------------
-- Track the attempts of a learner
--
-- Find an attempt by id
-- > SELECT * FROM learner.attempt WHERE id = ?
--
CREATE TABLE IF NOT EXISTS learner.attempt (
    id timeuuid,
    parent_id timeuuid,
    deployment_id timeuuid,
    courseware_element_id timeuuid,
    courseware_element_type text,
    student_id timeuuid,
    value int,
    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
