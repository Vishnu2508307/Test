--
-- publication.notification
-- --------------------
-- This table stores notification details.
--
-- Fetch the notification details with its id
-- > SELECT * FROM publication.notification WHERE id=?
--
CREATE TABLE IF NOT EXISTS publication.notification (
    id timeuuid,
    notification_type text,
    status text,
    message text,
    PRIMARY KEY (id, notification_type, status)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};