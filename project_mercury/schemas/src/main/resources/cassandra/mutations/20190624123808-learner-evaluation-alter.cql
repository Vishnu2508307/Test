-- cohortId field should be available in the evaluation table
ALTER TABLE learner.evaluation
  ADD cohort_id timeuuid
;
