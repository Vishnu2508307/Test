-- Adding a output_schema column to store a plugin properties which can be set by learner
ALTER TABLE plugin.manifest_by_plugin_version
  ADD output_schema text
;