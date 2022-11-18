Feature: Plugin permission events

  Background:
    Given a user with email "citrus@dev.dev" is created once
    And a list of workspace users
    | Alice    |
    | Bob      |
    | Charlie  |
    And "Alice" is logged in to the default client
    And "Bob" is logged via a "bob" client
    And "Alice" has created a plugin
    And "Alice" has shared a plugin with "Bob" as "REVIEWER"

  Scenario: Bob should be notified when Alice shares a plugin
    Given "Bob" subscribes to "plugin permission" events via a "bob" client
    When "Alice" shares this plugin with "Charlie"
    Then "Bob" should receive a plugin "PLUGIN_PERMISSION_GRANTED" notification via a "bob" client
    When "Alice" revokes "Charlie"'s plugin permission
    Then "Bob" should receive a plugin "PLUGIN_PERMISSION_REVOKED" notification via a "bob" client

  Scenario: Bob should be notified when Alice shares a plugin with a team
    Given "Alice" has created a "Marketing" team
    Given "Bob" subscribes to "plugin permission" events via a "bob" client
    When "Alice" shares this plugin with "Marketing" team
    Then "Bob" should receive a plugin "TEAM_PLUGIN_PERMISSION_GRANTED" notification via a "bob" client
    When "Alice" revokes "Marketing" team's plugin permission
    Then "Bob" should receive a plugin "TEAM_PLUGIN_PERMISSION_REVOKED" notification via a "bob" client
