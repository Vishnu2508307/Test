Feature: Create, replace, delete, move project feature

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"

  Scenario: It should allow a user to create a project
    When "Alice" creates a project named "INCEPTION" in workspace "one"
    Then project "INCEPTION" is created successfully

  Scenario: It should allow a user to replace a project name
    Given "Alice" has created project "INCEPTION" in workspace "one"
    When "Alice" replaces project "INCEPTION" name with "DECEPTION"
    Then the project name is replaced with "DECEPTION"

  Scenario: It should allow a user to delete a project
    Given "Alice" has created project "APOLLO" in workspace "one"
    When "Alice" deletes project "APOLLO"
    Then project "APOLLO" is deleted successfully

  Scenario: It should allow a user to move a project
    Given "Alice" has created project "APOLLO" in workspace "one"
    And "Alice" has created workspace "two"
    And an account "Bob" is created
    And an account "Charlie" is created
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over workspace "one"
    And "Alice" has created a "Pearson" team
    And "Bob" has created project "TRO" in workspace "one"
    And "Alice" adds "Charlie" to the "Pearson" team as "REVIEWER"
    And "Alice" has granted "REVIEWER" permission level to "team" "Pearson" over workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over workspace "two"
    And "Alice" has granted "REVIEWER" permission level to "team" "Pearson" over workspace "two"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "APOLLO"
    And "Alice" has granted "REVIEWER" permission level to "team" "Pearson" over project "APOLLO"
    When "Alice" move project "APOLLO" to workspace "two"
    Then project "APOLLO" is moved successfully
    And "Alice" projects list for workspace "one" is empty
    And "Alice" can list the following projects from workspace "two"
      | APOLLO |
    And "Bob" can list the following projects from workspace "one"
      | TRO |
    And "Bob" can list the following projects from workspace "two"
      | APOLLO |
    And "Charlie" projects list for workspace "one" is empty
    And "Charlie" can list the following projects from workspace "two"
      | APOLLO |
