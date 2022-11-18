Feature: Deleting a plugin

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created a plugin

  Scenario: The user should be able to delete a plugin, but the plugin still should be accessible through get messages
    Given "Alice" has published the plugin
    When "Alice" deletes a plugin
    Then the plugin is successfully deleted
    When "Alice" requests a plugin info in workspace
    Then "deletedAt" field is not empty
    When "Alice" requests a plugin in author
    Then "deletedAt" field is not empty

  Scenario: The user should not see deleted plugins in listings
    When "Alice" has deleted a plugin
    Then "Alice" can not see any plugin in workspace
    And "Alice" can not see any plugin in author

  Scenario: The user should not be able to share deleted plugin
    Given a workspace account "Bob" is created
    When "Alice" has deleted a plugin
    Then "Alice" cannot share the plugin with "Bob"

  Scenario: The user should not be able to list collaborators for deleted plugin
    When "Alice" has deleted a plugin
    Then "Alice" cannot list plugin collaborators

  Scenario: The user should not be able to publish deleted plugin
    Given "Alice" has deleted a plugin
    When "Alice" publishes the plugin
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "@startsWith('Plugin summary not found for id')@"

  Scenario: Only plugin owner should be able to delete the plugin
    Given a workspace account "Bob" is created
    When "Alice" shares this plugin with "Bob" as "CONTRIBUTOR"
    Then "Bob" can not delete the plugin
