-- add column to keep parent type to be consistent with other parent tables + fetch type from this fields instead of lifecycle field
ALTER TABLE courseware.scenario_by_parent
  ADD parent_type text
;
