-- add column to store the student_scope_urn for an interactive
ALTER TABLE courseware.interactive
  ADD student_scope_urn timeuuid
;
