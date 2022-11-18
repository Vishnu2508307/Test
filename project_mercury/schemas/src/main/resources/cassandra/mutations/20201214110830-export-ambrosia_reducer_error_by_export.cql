--
-- export.ambrosia_reducer_error_by_export
-- --------------------
-- Store ambrosia reducer errors that can be queried given an export id
--
-- Find all ambrosia reducer errors for an export
-- > SELECT * FROM export.ambrosia_reducer_error_by_export WHERE export_id=?
--
CREATE TABLE IF NOT EXISTS export.ambrosia_reducer_error_by_export (
    export_id timeuuid,
    cause text,
    error_message text,
    PRIMARY KEY (export_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
