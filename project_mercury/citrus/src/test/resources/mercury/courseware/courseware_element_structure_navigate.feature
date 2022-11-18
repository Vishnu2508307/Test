Feature: Fetch the courseware element navigate structure for an element

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "WIDGET_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "WIDGET_TWO" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "INPUT_ONE" component for the "SCREEN_TWO" interactive
    And "Alice" has created a "INPUT_TWO" component for the "SCREEN_TWO" interactive

    And "Alice" has created activity "UNIT_TWO" inside pathway "LINEAR_ONE"
    And "Alice" has created a "LINEAR_TWO" pathway for the "UNIT_TWO" activity
    And "Alice" has created a "LINEAR_TWO_SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "LINEAR_TWO_SCREEN_ONE_WIDGET_ONE" component for the "LINEAR_TWO_SCREEN_ONE" interactive
    And "Alice" has created a "LINEAR_TWO_SCREEN_ONE_WIDGET_TWO" component for the "LINEAR_TWO_SCREEN_ONE" interactive
    And "Alice" has created a "LINEAR_TWO_SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "LINEAR_TWO_SCREEN_TWO_WIDGET_ONE" component for the "LINEAR_TWO_SCREEN_TWO" interactive
    And "Alice" has created a "LINEAR_TWO_SCREEN_TWO_WIDGET_TWO" component for the "LINEAR_TWO_SCREEN_TWO" interactive

    And "Alice" has created activity "UNIT_THREE" inside pathway "LINEAR_TWO"
    And "Alice" has created a "LINEAR_THREE" pathway for the "UNIT_THREE" activity
    And "Alice" has created a "LINEAR_THREE_SCREEN_ONE" interactive for the "LINEAR_THREE" pathway
    And "Alice" has created a "LINEAR_THREE_SCREEN_TWO" interactive for the "LINEAR_THREE" pathway

  Scenario: It should allow a user with reviewer permission to fetch a courseware element structure navigate
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" fetches the courseware element structure navigate for "ACTIVITY" "UNIT_ONE"
    Then the courseware element structure navigate is returned successfully for "UNIT_ONE" with children status "TRUE"

  Scenario: It should allow the creator user to fetch a courseware element structure navigate with one level
    When "Alice" fetches the courseware element structure navigate for "ACTIVITY" "UNIT_ONE"
    Then the courseware element structure navigate is returned successfully for "UNIT_ONE" with children status "TRUE"

  Scenario: It should allow the creator user to fetch a courseware element structure navigate with one level
    When "Alice" fetches the courseware element structure navigate for "ACTIVITY" "UNIT_TWO"
    Then the courseware element structure navigate is returned successfully for "UNIT_TWO" with children status "TRUE"

  Scenario: It should allow the creator user to fetch a courseware element structure navigate with one level and no children
    When "Alice" fetches the courseware element structure navigate for "ACTIVITY" "UNIT_THREE"
    Then the courseware element structure navigate is returned successfully for "UNIT_THREE" with no children

  Scenario: It should allow a user with reviewer permission to fetch a courseware element structure navigate with one level and no children
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" fetches the courseware element structure navigate for "ACTIVITY" "UNIT_THREE"
    Then the courseware element structure navigate is returned successfully for "UNIT_THREE" with no children