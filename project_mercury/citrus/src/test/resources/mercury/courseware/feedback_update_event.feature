Feature: Event messages should be emitted for the activities on feedback actions

  Background:
    Given a workspace account "Alice" is created
    And "Alice" is logged in to the default client
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"

  Scenario: The event should be emitted on feedback create
    Given "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created a "ONE" feedback for the "SCREEN" interactive
    Then "Bob" should receive an action "CREATED" message for the "ONE" feedback

  Scenario: The event should be emitted on feedback delete
    Given "Alice" has created a "ONE" feedback for the "SCREEN" interactive
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has deleted the "ONE" feedback for the "SCREEN" interactive
    Then "Bob" should receive an action "DELETED" message for the "ONE" feedback

  Scenario: The event should be emitted on feedback update
    Given "Alice" has created a "ONE" feedback for the "SCREEN" interactive
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has updated the "ONE" feedback config with "feedback config"
    Then "Bob" should receive an action "CONFIG_CHANGE" message for the "ONE" feedback with "config=feedback config"
