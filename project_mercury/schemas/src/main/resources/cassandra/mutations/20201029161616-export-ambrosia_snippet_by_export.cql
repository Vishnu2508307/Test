--
-- export.ambrosia_snippet_by_export
-- ---------------------------------
-- This table store the ambrosia snippet by export courseware
--
-- Find the details of ambrosia snippet by export
-- > SELECT * FROM export.ambrosia_snippet_by_export WHERE export_id=?
--
CREATE TABLE IF NOT EXISTS export.ambrosia_snippet_by_export (
    export_id timeuuid,
    element_id timeuuid,
    element_type text,
    account_id timeuuid,
    ambrosia_snippet text,
    notification_id timeuuid,
    primary key (export_id, notification_id)
) WITH CLUSTERING ORDER BY (notification_id DESC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
