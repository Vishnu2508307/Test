Feature: Event messages should be emitted for the activities on interactive actions

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
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"

  Scenario: The event should be emitted on interactive create
    Given "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    Then "Bob" should receive an action "CREATED" message for the "SCREEN" interactive and "LINEAR" parent pathway

  Scenario: The event should be emitted on interactive delete
    Given "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has deleted the "SCREEN" interactive for the "LINEAR" pathway
    Then "Bob" should receive an action "DELETED" message for the "SCREEN" interactive and "LINEAR" parent pathway

  Scenario: The event should be emitted on interactive update
    Given "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has updated the "SCREEN" interactive config with "some config"
    Then "Bob" should receive an action "CONFIG_CHANGE" message for the "SCREEN" interactive with "config=some config"

  Scenario: The event should be emitted on reorder interactive scenarios
    Given "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a scenario "FAIL" for the "SCREEN" interactive
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has reordered the "SCREEN" interactive scenarios to
      | FAIL |
    Then "Bob" should receive an action "SCENARIO_REORDERED" message for the "SCREEN" interactive

  Scenario: It should broadcast content when an interactive is duplicated
    Given "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" duplicates "SCREEN" interactive into "LINEAR" pathway
    Then the "SCREEN_COPY" interactive has been successfully duplicated
    And "Bob" should receive an action "DUPLICATED" message for the "SCREEN_COPY" interactive and "LINEAR" parent pathway

  Scenario: It should broadcast content when an evaluable is set
    Given "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created evaluable for "SCREEN" "INTERACTIVE" with evaluation mode "COMBINED"
    Then "Bob" should receive an action "EVALUABLE_SET" message for the "SCREEN" interactive