-- Alter table to add element_id column to store a list of text
ALTER TABLE math.usage_by_asset_urn ADD element_id list<text>;
