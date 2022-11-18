---
--- It was decided to rename 'group' to 'team' so this old table iam_global.plugin_permission_by_group should be deleted
--- No replacing table will be created for now. As we will use only iam_global.plugin_permission_by_team to define team
--- permissions for a plugin
---
DROP TABLE IF EXISTS iam_global.group_by_plugin_permission;
