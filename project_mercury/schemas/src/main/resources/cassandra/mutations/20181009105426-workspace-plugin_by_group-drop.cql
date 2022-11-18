---
--- It was decided to rename 'group' to 'team' so this old table workspace.plugin_by_group should be deleted
--- and new table plugin.plugin_by_team will be created instead.
---
DROP TABLE IF EXISTS workspace.plugin_by_group;
