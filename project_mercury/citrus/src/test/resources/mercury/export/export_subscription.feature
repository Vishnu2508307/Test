Feature: Subscriptions for courseware export

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"

  Scenario: A workspace user can subscribe to export messages
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE"
    When "Bob" subscribes to the export for activity "UNIT_ONE"
    Then "Bob" is successfully subscribed to the export

  Scenario: A workspace user can unsubscribe to export messages
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE"
    When "Bob" subscribes to the export for activity "UNIT_ONE"
    Then "Bob" is successfully subscribed to the export
    When "Bob" unsubscribes to the export for activity "UNIT_ONE"
    Then "Bob" is successfully unsubscribed to the export
