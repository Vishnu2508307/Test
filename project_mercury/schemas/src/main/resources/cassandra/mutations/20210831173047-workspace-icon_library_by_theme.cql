--
-- workspace.icon_library_by_theme
-- --------------------
-- Save icon libraries for each theme
--
-- Find all the icon library names for a theme
-- > SELECT * FROM workspace.icon_library_by_theme WHERE theme_id = ?
--
CREATE TABLE IF NOT EXISTS workspace.icon_library_by_theme (
    theme_id timeuuid,
    icon_library text,
    status text,
    PRIMARY KEY (theme_id, icon_library)
)
WITH compression = {
    'sstable_compression' : 'SnappyCompressor'
};
