-- Adding a tags column to store a list of text tags
ALTER TABLE plugin.manifest_by_plugin_version
  ADD tags list<text>
;
