CREATE TABLE IF NOT EXISTS changelog.db (
    id          text,
    on_keyspace text,
    on_table    text,

    PRIMARY KEY (id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
