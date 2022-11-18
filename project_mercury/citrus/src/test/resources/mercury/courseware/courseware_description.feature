Feature: To set description for a courseware element

  Background:
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin

  Scenario: It should not allow a user without proper permission level to set description of a courseware element
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    When "Bob" set courseware description "Sample" for "ACTIVITY_ONE" and type "ACTIVITY"
    Then "Bob" is not allow to set description on the courseware

  Scenario: It should allow contributor to set description of a courseware element
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    When "Alice" set courseware description "Sample" for "ACTIVITY_ONE" and type "ACTIVITY"
    Then the "Sample" description for "ACTIVITY_ONE" and type "ACTIVITY" is set by "Alice"

  Scenario: It should allow contributor to get the description of an activity
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    And "Alice" has added description "Sample" for "ACTIVITY_ONE" "ACTIVITY"
    When "Alice" fetches the "ACTIVITY_ONE" activity
    Then the "ACTIVITY_ONE" activity includes the "Sample" description

  Scenario: It should allow contributor to get the description of a component
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has added a "PROGRESS" component to the "UNIT_ONE" activity
    And "Alice" has added description "Test" for "PROGRESS" "COMPONENT"
    When "Alice" fetches the "PROGRESS" component
    Then the "PROGRESS" component includes the "Test" description

  Scenario: It should allow contributor to get the description of an interactive
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has added description "Sample" for "SCREEN" "INTERACTIVE"
    When "Alice" fetches the "SCREEN" interactive
    Then the "SCREEN" interactive includes the "Sample" description

  Scenario: It should allow contributor to get the description of a pathway
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT_ONE" activity
    And "Alice" has added description "Sample" for "LINEAR" "PATHWAY"
    When "Alice" fetches the "LINEAR" pathway
    Then the "LINEAR" pathway includes the "Sample" description