--
-- Modify the plugin.summary to contain guide column
--
ALTER TABLE plugin.summary
  ADD latest_guide text
;