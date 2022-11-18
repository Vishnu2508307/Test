-- Removing change_id as not required
ALTER TABLE plugin.learnspace_log_statements_by_plugin
    DROP change_id
;
