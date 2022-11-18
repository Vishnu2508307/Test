Feature: Fetching a project

  Background:
    Given an account "Alice" is created
    And an account "Bob" is created
    And an account "Chuck" is created
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: It should allow a user with REVIEWER or higher permission level over the project to fetch the project
    Given "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    Then "Bob" is able to fetch project "TRO"

  Scenario: It should not allow a user to fetch a project without proper permission
    When "Chuck" fetches project "TRO"
    Then the project is not fetched due to missing proper permission