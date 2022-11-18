--
-- Modify the plugin.summary to contain latest_version_release_date information
--
ALTER TABLE plugin.summary
  ADD latest_version_release_date bigint
;
