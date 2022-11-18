--
-- courseware.changelog_by_element
-- --------------------
-- This table is designed to record the courseware changelog by element.
--
-- Find the change log details by element id.
-- > SELECT * FROM courseware.changelog_by_element WHERE ...
--
CREATE TABLE IF NOT EXISTS courseware.changelog_by_element
(
    element_id timeuuid,
    courseware_action text,
    account_id timeuuid,
    on_element_id timeuuid,
    on_element_type text,
    on_parent_walkable_id timeuuid,
    on_parent_walkable_type text,
    id timeuuid,
    primary key (element_id, id)
)
    with clustering order by (id desc)
    and compression = {
    'sstable_compression' : 'SnappyCompressor'
    };
