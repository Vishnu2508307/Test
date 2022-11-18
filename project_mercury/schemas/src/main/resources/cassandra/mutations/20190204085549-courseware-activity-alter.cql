-- add column to store the student_scope_urn for an activity
ALTER TABLE courseware.activity
  ADD student_scope_urn timeuuid
;
