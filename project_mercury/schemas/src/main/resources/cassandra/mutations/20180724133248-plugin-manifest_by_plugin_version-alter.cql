--
-- Modify the plugin.manifest_by_plugin_version to contain the whats new column
--
ALTER TABLE plugin.manifest_by_plugin_version
  ADD whats_new text
;
