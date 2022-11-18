-- add column to store the student_scope_urn for a learner interactive
ALTER TABLE learner.interactive
  ADD student_scope_urn timeuuid
;
