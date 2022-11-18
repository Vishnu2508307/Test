-- Removing link to the parent activity. The separate table to keep relationships will be created
ALTER TABLE courseware.pathway
  DROP activity_id
;
