--
-- cohort.cohort_by_lms_course
-- --------------------
-- Associates cohorts by LMS course ids
--
-- Find the cohort associated to a LMS course id
-- > SELECT cohort_id FROM cohort.cohort_by_lms_course WHERE lms_course_id = ?
--
CREATE TABLE IF NOT EXISTS cohort.cohort_by_lms_course (
    lms_course_id text,
    cohort_id timeuuid,
    PRIMARY KEY (lms_course_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
