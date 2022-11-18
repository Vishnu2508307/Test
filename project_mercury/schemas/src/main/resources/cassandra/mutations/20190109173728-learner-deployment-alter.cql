-- Add the cohort_id field so that the relationship with the deployment is available
ALTER TABLE learner.deployment
  ADD cohort_id timeuuid
;
