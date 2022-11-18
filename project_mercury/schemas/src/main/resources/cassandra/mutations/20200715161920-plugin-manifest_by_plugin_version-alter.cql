--
-- Modify the plugin.manifest_by_plugin_version to contain guide column
--
ALTER TABLE plugin.manifest_by_plugin_version
  ADD guide text
;
