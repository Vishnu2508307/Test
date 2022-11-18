--
-- Add a column to store a stringified json array of the triggered actions during an evaluation
--
ALTER TABLE learner.evaluation
  ADD triggered_actions text
;
