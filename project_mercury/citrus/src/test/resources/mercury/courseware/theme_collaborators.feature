Feature: Listing users who have access to theme

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" created theme "theme_one" for workspace "one"

  Scenario: User should be able to fetch a list of all collaborators who have access to a theme
    Given "Alice" grants "REVIEWER" permission level for theme "theme_one" to "account"
      | Bob     |
      | Charlie |
    Then the following "account" have "REVIEWER" permission level over theme "theme_one"
      | Bob     |
      | Charlie |
    Given "Alice" has created a "Marketing" team
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level for theme "theme_one" to "team"
      | Marketing |
      | Sales     |
    Then the following "team" have "REVIEWER" permission level over theme "theme_one"
      | Marketing |
      | Sales     |
    When "Alice" fetches the accounts for theme "theme_one"
    Then the theme collaborators list contains
      | Bob       | account |
      | Charlie   | account |
      | Alice     | account |
      | Marketing | team    |
      | Sales     | team    |

    Scenario: User should be able to limit a list of theme accounts
      Given "Alice" grants "REVIEWER" permission level for theme "theme_one" to "account"
        | Bob     |
        | Charlie |
      Then the following "account" have "REVIEWER" permission level over theme "theme_one"
        | Bob     |
        | Charlie |
      Given "Alice" has created a "Marketing" team
      And "Alice" has created a "Sales" team
      And "Alice" grants "REVIEWER" permission level for theme "theme_one" to "team"
        | Marketing |
        | Sales     |
      Then the following "team" have "REVIEWER" permission level over theme "theme_one"
        | Marketing |
        | Sales     |
      When "Alice" fetches the accounts for theme "theme_one" with limit 3
      Then the theme collaborators list contains
        | Marketing | team    |
        | Sales     | team    |
