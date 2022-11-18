-- Store thumbnail and tags in summary table
ALTER TABLE plugin.summary
  ADD (thumbnail text, tags list<text>)
;
