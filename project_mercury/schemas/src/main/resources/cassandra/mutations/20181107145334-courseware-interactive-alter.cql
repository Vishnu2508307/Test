--Removing link to the parent pathway. The separate table to keep relationships will be created
ALTER TABLE courseware.interactive
  DROP pathway_id
;
