-- add column to store the student_scope_urn for a learner activity
ALTER TABLE learner.activity
  ADD student_scope_urn timeuuid
;
