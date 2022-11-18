Feature: Fetch the courseware element structure for an element

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

  Scenario: It should not allow a user with reviewer permission to fetch a courseware root element structure
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" fetches the courseware element structure for "ACTIVITY" "UNIT_ONE"
    Then the courseware element structure is not returned due to missing permission level

  Scenario: It should allow a user with contributor permission to fetch a courseware root element structure
    Given an account "Bob" is created
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    When "Bob" fetches the courseware element structure for "ACTIVITY" "UNIT_ONE"
    Then the courseware root element structure is returned successfully for "UNIT_ONE"

  Scenario: It should allow the creator user to fetch a courseware root element structure
    When "Alice" fetches the courseware element structure for "ACTIVITY" "UNIT_ONE"
    Then the courseware root element structure is returned successfully for "UNIT_ONE"

  Scenario: It should not allow a user with reviewer permission to fetch a courseware non root element structure
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" fetches the courseware element structure for "INTERACTIVE" "SCREEN_ONE"
    Then the courseware element structure is not returned due to missing permission level

  Scenario: It should allow a user with contributor permission to fetch a courseware non root element structure
    Given an account "Bob" is created
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    When "Bob" fetches the courseware element structure for "INTERACTIVE" "SCREEN_ONE"
    Then the courseware element structure is returned successfully for "SCREEN_ONE"

  Scenario: It should allow the creator user to fetch a courseware non root element structure
    When "Alice" fetches the courseware element structure for "INTERACTIVE" "SCREEN_ONE"
    Then the courseware element structure is returned successfully for "SCREEN_ONE"

  Scenario: It should allow a user with contributor permission to fetch a courseware non root element structure with config fields
    Given an account "Bob" is created
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has saved config for "SCREEN_ONE" interactive "titleField"
    When "Bob" fetches the courseware element structure for "INTERACTIVE" "SCREEN_ONE" with field "titleField"
    Then the courseware element structure is returned successfully for "SCREEN_ONE" with fields

  Scenario: It should allow a user with contributor permission to fetch a courseware root element structure with config fields
    Given an account "Bob" is created
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created configuration for "UNIT_ONE" with field name "titleField" and field value "titleValue"
    When "Bob" fetches the courseware element structure for "ACTIVITY" "UNIT_ONE" with field "titleField"
    Then the courseware root element structure is returned successfully for "UNIT_ONE" with fields
