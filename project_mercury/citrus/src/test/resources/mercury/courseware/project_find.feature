Feature: Test to find project information for given element and type

  Background:
    Given an account "Alice" is created
    And an account "Bob" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway

  Scenario: User should be able find project information for given element
    When "Alice" tries to fetch project info for element "UNIT_ONE" and element type "ACTIVITY"
    Then "Alice" fetch following project information successfully
      | projectName   | TRO |
      | workspaceName | one |

  Scenario: User cannot find project information with invalid or missing permission
    When "Bob" tries to fetch project info for element "UNIT_ONE" and element type "ACTIVITY"
    Then the project successfully is not fetched successfully

  Scenario: User with valid permission should be able to find project information
    When "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" tries to fetch project info for element "SCREEN_ONE" and element type "INTERACTIVE"
    Then "Bob" fetch following project information successfully
      | projectName   | TRO |
      | workspaceName | one |
