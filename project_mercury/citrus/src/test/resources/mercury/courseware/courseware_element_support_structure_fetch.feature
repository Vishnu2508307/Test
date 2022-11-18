Feature: Fetch the courseware element structure for an element

  Background:
    Given an account "Alice" is created
    And a support role account "Bob" exists
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

  Scenario: A non-support role user should not be able to fetch a courseware support root element structure
    When "Alice" fetches the courseware support element structure for "ACTIVITY" "UNIT_ONE"
    Then the courseware support element structure is not returned due to missing permission level

  Scenario: It should allow a support user to fetch a courseware root element structure
    When "Bob" fetches the courseware support element structure for "ACTIVITY" "UNIT_ONE"
    Then the courseware support root element structure is returned successfully for "UNIT_ONE"

  Scenario: A non-support role user should not be able to fetch a courseware non root element structure
    When "Alice" fetches the courseware support element structure for "INTERACTIVE" "SCREEN_ONE"
    Then the courseware support element structure is not returned due to missing permission level

  Scenario: It should allow a support user to fetch a courseware non root element structure
    When "Bob" fetches the courseware support element structure for "INTERACTIVE" "SCREEN_ONE"
    Then the courseware support element structure is returned successfully for "SCREEN_ONE"