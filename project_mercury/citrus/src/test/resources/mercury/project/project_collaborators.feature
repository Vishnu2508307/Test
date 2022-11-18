Feature: Listing users who have access to projects

  Background:
    Given an account "Alice" is created
    And an account "Bob" is created
    And "Alice" has created a "Marketing" team
    And "Alice" has created workspace "one"
    And "Alice" has created project "Pearson" in workspace "one"

  Scenario: User should be able to fetch a list of all collaborators who have access to a project
    Given "Alice" has granted "CONTRIBUTOR" permission level to "team" "Marketing" over project "Pearson"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "Pearson"
    When "Alice" fetches the accounts for project "Pearson"
    Then the project collaborators list contains
      | Alice     | account |
      | Bob       | account |
      | Marketing | team    |
    When "Bob" fetches the accounts for project "Pearson"
    Then the project collaborators list contains
      | Alice     | account |
      | Bob       | account |
      | Marketing | team    |

  Scenario: User should be able to fetch a list of all collaborators who have access to a project with limit
    Given "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "Pearson"
    When "Alice" fetches the accounts for project "Pearson" with limit 1
    Then the project collaborators list contains
      | Alice      | account |