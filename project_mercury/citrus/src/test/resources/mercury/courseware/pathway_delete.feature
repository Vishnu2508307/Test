Feature: Delete a pathway

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: A user should be able to delete a pathway
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    When "Alice" deletes the "LINEAR" pathway for the "UNIT" activity
    Then the "LINEAR" pathway is deleted
    And the "UNIT" activity does not have the "LINEAR" pathway as child

  Scenario: A user should not be able to fetch a deleted pathway
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has deleted the "LINEAR" pathway for the "UNIT" activity
    Then "Alice" can not fetch "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
