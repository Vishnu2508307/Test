--
-- Modify the plugin.manifest_by_plugin_version to contain the website url column
--
ALTER TABLE plugin.manifest_by_plugin_version
  ADD website_url text
;
