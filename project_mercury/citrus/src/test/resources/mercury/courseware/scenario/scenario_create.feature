Feature: Creation a Scenario

  Background:
    Given a workspace account "Alice" is created
    Given "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "activity" inside project "TRO"

  Scenario: It should not allow to create a scenario when the name is not supplied
    When "Alice" creates a scenario with
      | lifecycle | ACTIVITY_EVALUATE |
    Then the scenario is not created due to message "name is required" and code 400

  Scenario: It should not allow to create a scenario when the lifecycle is not supplied
    When "Alice" creates a scenario with
      | name | scenario 1 |
    Then the scenario is not created due to message "lifecycle is required" and code 400

  Scenario: It should not allow to create a scenario when the lifecycle value is invalid
    When "Alice" creates a scenario with
      | name      | scenario 1 |
      | lifecycle | invalid    |
    Then the scenario is not created due to message "Invalid message format: '[lifecycle]' has invalid format" and code 400

  Scenario: It should allow a user to create a valid scenario
    When "Alice" creates a scenario with
      | name        | scenario 1              |
      | lifecycle   | ACTIVITY_EVALUATE       |
      | description | a brilliant description |
      | condition   | condition               |
      | actions     | actions                 |
      | correctness | correct                 |
    Then the scenario is successfully created

  Scenario: It should allow a user to update an existing scenario
    Given "Alice" has created scenario with
      | name        | scenario 1              |
      | lifecycle   | ACTIVITY_EVALUATE       |
      | description | a brilliant description |
      | condition   | condition               |
      | actions     | actions                 |
      | correctness | correct                 |
    When "Alice" updates scenario with
      | name        | updated scenario 1  |
      | condition   | updated_condition   |
      | actions     | updated_actions     |
      | correctness | none                |
    Then the updated scenario has fields
      | name        | updated scenario 1  |
      | lifecycle   | ACTIVITY_EVALUATE   |
      | condition   | updated_condition   |
      | actions     | updated_actions     |
      | correctness | none                |
    Then the list of scenarios for the activity and lifecycle ACTIVITY_EVALUATE contains updated scenario
      | name        | updated scenario 1  |
      | lifecycle   | ACTIVITY_EVALUATE   |
      | condition   | updated_condition   |
      | actions     | updated_actions     |
      | correctness | none                |

  Scenario: It should not allow to create a scenario when the user doesn't have CONTRIBUTOR permissions on workspace
    Given a workspace account "Bob" is created
    Then "Bob" can not create a scenario due to message "Unauthorized: Unauthorized permission level" and code 401
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can create a scenario successfully

  Scenario: It should allow a user to create a valid grade pass back scenario
    When "Alice" creates a scenario with
      | name        | scenario 1              |
      | lifecycle   | ACTIVITY_EVALUATE       |
      | description | a brilliant description |
      | condition   | condition               |
      | gradePassBackActions | gradePassBackActions |
      | correctness | correct                 |
    Then the scenario is successfully created
