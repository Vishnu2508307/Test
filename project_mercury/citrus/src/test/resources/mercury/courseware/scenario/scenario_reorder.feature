Feature: Reorder scenarios

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "activity" inside project "TRO"

  Scenario: A workspace user should be able to reorder a list of lifecycle scenarios that belongs to an interactive
    Given "Alice" has created a pathway
    And "Alice" has created an interactive
    And "Alice" has created interactive scenario "one"
    And "Alice" has created interactive scenario "two"
    When she reorders the scenarios for the created "interactive" as
      | two |
      | one |
    Then scenarios are successfully ordered as
      | two |
      | one |

  Scenario: Only workspace CONTRIBUTOR can reorder scenarios
    Given a workspace account "Bob" is created
    Then "Bob" can not reorder scenarios for the activity due to message "Unauthorized: Unauthorized permission level" and code 401
    When "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can not reorder scenarios for the activity due to message "Unauthorized: Unauthorized permission level" and code 401
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can not reorder scenarios for the activity successfully
