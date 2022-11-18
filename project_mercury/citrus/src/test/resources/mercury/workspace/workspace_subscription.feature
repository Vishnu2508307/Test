Feature: Subscriptions for workspace

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" has created and published course plugin

  Scenario: A user can subscribe/unsubscribe to workspace with REVIEWER permission
    Given "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    Then "Bob" subscribe to the workspace "one" via a "Bob" client successfully
    Then "Bob" unsubscribe to the workspace "one" via a "Bob" client successfully

  Scenario: A user can subscribe/unsubscribe to workspace with CONTRIBUTOR permission
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    Then "Bob" subscribe to the workspace "one" via a "Bob" client successfully
    Then "Bob" unsubscribe to the workspace "one" via a "Bob" client successfully

  Scenario: It should broadcast message when a project is created to the workspace
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" subscribe to the workspace "one" via a "Bob" client successfully
    And "Alice" has created project "TRO" in workspace "one"
    Then "Bob" should receive an action "CREATED" for project "TRO" and workspace "one"

  Scenario: A user can not subscribe/unsubscribe to workspace without permission
    Given a workspace account "Charlie" is created
    And "Charlie" is logged via a "Charlie" client
    Then "Charlie" cannot subscribe to the workspace "one" via a "Bob" client due to missing permission level
    Then "Charlie" cannot unsubscribe to the workspace "one" via a "Bob" client due to missing permission level
