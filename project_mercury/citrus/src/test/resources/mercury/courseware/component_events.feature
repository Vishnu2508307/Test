Feature: Event messages should be emitted on the tree structure on component actions

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" is logged in to the default client
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity

  Scenario: It should emit events when an activity component is created
    Given "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created a "PROGRESS" component for the "LESSON" activity
    Then "Bob" should receive an action "CREATED" message for the "PROGRESS" component

  Scenario: It should emit events when an interactive component is created
    Given "Alice" has created a "SCREEN_ONE" interactive for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created a "PROGRESS" component for the "SCREEN_ONE" interactive
    Then "Bob" should receive an action "CREATED" message for the "PROGRESS" component

  Scenario: It should emit events when an activity component is deleted
    Given "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Alice" has created a "PROGRESS" component for the "LESSON" activity
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has deleted the "PROGRESS" component for the "LESSON" activity
    Then "Bob" should receive an action "DELETED" message for the "PROGRESS" component

  Scenario: It should emit events when an interactive component is deleted
    Given "Alice" has created a "SCREEN_ONE" interactive for the "COURSE_LINEAR" pathway
    And "Alice" has created a "PROGRESS" component for the "SCREEN_ONE" interactive
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has deleted the "PROGRESS" component for the "SCREEN_ONE" interactive
    Then "Bob" should receive an action "DELETED" message for the "PROGRESS" component

  Scenario: It should emit events when a component config is replaced
    Given "Alice" has created a "SCREEN_ONE" interactive for the "COURSE_LINEAR" pathway
    And "Alice" has created a "PROGRESS" component for the "SCREEN_ONE" interactive
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" replace the "PROGRESS" component config with
    """
    {"title":"Demo Component", "desc":"some nice component"}
    """
    Then "Bob" should receive an action "CONFIG_CHANGE" message for the "PROGRESS" component

  Scenario: It should emit events when an interactive component is moved
    Given "Alice" has created a "SCREEN_ONE" interactive for the "COURSE_LINEAR" pathway
    Given "Alice" has created a "SCREEN_TWO" interactive for the "COURSE_LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has moved the "SCREEN_ONE" component to "SCREEN_TWO" "interactive"
      | COMPONENT_ONE |
    Then "Bob" should receive an action "COMPONENT_MOVED" message for the "COMPONENT_ONE" "component"
