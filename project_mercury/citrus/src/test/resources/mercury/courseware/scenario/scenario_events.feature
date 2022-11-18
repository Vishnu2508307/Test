Feature: Event messages should be emitted on the tree structure on scenario actions

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" is logged in to the default client
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "COURSE_LINEAR" pathway

  Scenario: It should emit an event when a scenario is created
    Given "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created scenario "SUCCESS" for activity "LESSON_ONE"
    Then "Bob" should receive an action "CREATED" message for the "SUCCESS" scenario

  Scenario: It should emit an event when a scenario is updated
    Given "Alice" has created scenario "SUCCESS" for activity "LESSON_ONE"
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has updated the "SUCCESS" scenario
    Then "Bob" should receive an action "UPDATED" message for the "SUCCESS" scenario

  Scenario: It should emit an event when scenarios are reordered
    Given "Alice" has created scenario "SUCCESS" for activity "LESSON_ONE"
    And "Alice" has created scenario "FAIL" for activity "LESSON_ONE"
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has reordered the "LESSON_ONE" scenarios to
     | FAIL |
     | SUCCESS |
    Then "Bob" should receive an action scenario "SCENARIO_REORDERED" message for the "LESSON_ONE" activity
