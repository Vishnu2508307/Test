Feature: Test evaluable set and get functionalities

  Background:
    Given an account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: User should be able to set evaluable successfully
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Alice" creates evaluable for "SCREEN_ONE" "INTERACTIVE" with evaluation mode "DEFAULT"
    Then the evaluable has been created successfully

  Scenario: User without proper permission level should not be able to set evaluable
    Given an account "Bob" is created
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates evaluable for "SCREEN_ONE" "INTERACTIVE" with evaluation mode "DEFAULT"
    Then the evaluable is not created due to permission issue

  Scenario: User with proper permission level should be able to fetch an evaluable
    Given an account "Bob" is created
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created evaluable for "SCREEN_ONE" "INTERACTIVE" with evaluation mode "COMBINED"
    When "Bob" tries to fetch evaluable for "SCREEN_ONE" "INTERACTIVE"
    Then the evaluable fetch failed due to permission issue
    When "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" tries to fetch evaluable for "SCREEN_ONE" "INTERACTIVE"
    Then "Bob" can fetch following data from "SCREEN_ONE" evaluable
      | elementType    | INTERACTIVE   |
      | evaluationMode | COMBINED      |
