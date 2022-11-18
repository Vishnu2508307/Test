Feature: Subscriptions for project

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: A workspace user can subscribe/unsubscribe to project with REVIEWER permission
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" subscribes to the project "TRO"
    Then "Bob" is successfully subscribed to the project
    When "Bob" unsubscribes to the project "TRO"
    Then "Bob" is successfully unsubscribed to the project

  Scenario: A workspace user can subscribe/unsubscribe to project with CONTRIBUTOR permission
    Given an account "Bob" is created
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    When "Bob" subscribes to the project "TRO"
    Then "Bob" is successfully subscribed to the project
    When "Bob" unsubscribes to the project "TRO"
    Then "Bob" is successfully unsubscribed to the project

  Scenario: A workspace user can not subscribe/unsubscribe to project without permission
    Given an account "Bob" is created
    When "Bob" subscribes to the project "TRO"
    Then mercury should respond with: "workspace.project.subscribe.error" and code "401"
    When "Bob" unsubscribes to the project "TRO"
    Then mercury should respond with: "workspace.project.unsubscribe.error" and code "401"
