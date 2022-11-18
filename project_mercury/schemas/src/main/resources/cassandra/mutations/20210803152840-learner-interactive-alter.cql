--
-- Adding evaluation_mode to interactive table so that the evaluable table can be deprecated
-- and a call spared when fetching the evaluation_mode
--
ALTER TABLE learner.interactive
    ADD evaluation_mode text
;
