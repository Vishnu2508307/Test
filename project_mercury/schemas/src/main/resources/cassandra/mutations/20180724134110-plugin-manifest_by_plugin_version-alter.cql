--
-- Modify the plugin.manifest_by_plugin_version to contain the support url column
--
ALTER TABLE plugin.manifest_by_plugin_version
  ADD support_url text
;
