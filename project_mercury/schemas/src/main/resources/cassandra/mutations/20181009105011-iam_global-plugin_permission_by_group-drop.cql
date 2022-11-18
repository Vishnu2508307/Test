---
--- It was decided to rename 'group' to 'team' so this old table iam_global.plugin_permission_by_group should be deleted
--- and new table iam_global.plugin_permission_by_team will be created instead.
---
DROP TABLE IF EXISTS iam_global.plugin_permission_by_group;