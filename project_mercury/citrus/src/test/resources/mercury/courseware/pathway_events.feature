Feature: Event messages should be emitted on the tree structure on pathway actions

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" is logged in to the default client
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "COURSE" inside project "TRO"

  Scenario: It should emit events when a pathway is created
    Given "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    And "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    Then "Bob" should receive an action "CREATED" message for the "COURSE_LINEAR" pathway

  Scenario: It should emit events when a pathway is deleted
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has deleted the "COURSE_LINEAR" pathway for the "COURSE" activity
    Then "Bob" should receive an action "DELETED" message for the "COURSE_LINEAR" pathway

  Scenario: It should emit events when a pathway config is replaced
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has replaced config for "COURSE_LINEAR" pathway with
    """
      {"foo":"bar"}
    """
    Then "Bob" should receive an action "CONFIG_CHANGE" message for the "COURSE_LINEAR" pathway

