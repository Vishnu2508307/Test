-- cohort_id should be available in the evaluation_by_attempt_id table
ALTER TABLE learner.evaluation_by_attempt_id
  ADD cohort_id timeuuid
;
