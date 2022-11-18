--
-- learner.document_source_by_asset
-- --------------------
-- Stores the location of a document type asset. Unlike multimedia types, only original asset is referenced.
--
-- fetch source of document for a given asset
-- > SELECT * FROM learner.document_source_by_asset WHERE asset_id = '1234-...-456789'
--
CREATE TABLE IF NOT EXISTS learner.document_source_by_asset (
    asset_id UUID,
    url text,
    PRIMARY KEY (asset_id)
)
WITH compression = { 
    'sstable_compression' : 'SnappyCompressor'
};
