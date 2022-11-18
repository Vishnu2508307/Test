--Removing link to the parent interactive. The separate table to keep relationships will be created
ALTER TABLE courseware.component
  DROP interactive_id
;
