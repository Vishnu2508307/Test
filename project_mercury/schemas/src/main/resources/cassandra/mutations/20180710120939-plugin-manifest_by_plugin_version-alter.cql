--
-- Modify the plugin.manifest_by_plugin_version to contain plugin name for specific version
--
ALTER TABLE plugin.manifest_by_plugin_version
  ADD name text
;
