--
-- courseware.changelog_by_project
-- --------------------
-- This table is designed to record the courseware changelog by project.
--
-- Find the change log details by project id. 
-- > SELECT * FROM courseware.changelog_by_project WHERE project_id= ?
--
CREATE TABLE IF NOT EXISTS courseware.changelog_by_project
(
    project_id timeuuid,
    courseware_action text,
    account_id  timeuuid,
    on_element_id timeuuid,
    on_element_type text,
    on_parent_walkable_id timeuuid,
    on_parent_walkable_type text,
    id timeuuid,
    primary key (project_id, id)
)
    with clustering order by (id desc)
    and compression = {
    'sstable_compression' : 'SnappyCompressor'
    };