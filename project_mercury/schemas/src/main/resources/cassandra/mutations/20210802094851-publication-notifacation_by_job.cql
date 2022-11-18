--
-- publication.notifacation_by_job
-- --------------------
-- This table stores the mapping of jobSummary and notification.
--
-- Fetch the notification id with the jobSummary id.
-- > SELECT * FROM publication.notification_by_job WHERE job_id=?
--
CREATE TABLE IF NOT EXISTS publication.notification_by_job (
   job_id timeuuid,
   notification_id timeuuid,
   PRIMARY KEY (job_id, notification_id)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
