--
-- Adding evaluation_mode to activity table so that the evaluable table can be deprecated
-- and a call spared when fetching the evaluation_mode
--
ALTER TABLE courseware.activity
  ADD evaluation_mode text
;
