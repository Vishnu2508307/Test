---
--- Adding column export_id
---
ALTER TABLE export.result_notification
  ADD export_id timeuuid;
