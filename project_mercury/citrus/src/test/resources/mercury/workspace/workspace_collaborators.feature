Feature: Listing users who have access to workspace

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "SmartSparrow"
    And "Alice" has created a "Marketing" team

  Scenario: User should be able to fetch a list of all collaborators who have access to a workspace
    Given "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "SmartSparrow"
    And "Alice" has granted "CONTRIBUTOR" permission level to "team" "Marketing" over workspace "SmartSparrow"
    When "Alice" fetches the accounts for workspace "SmartSparrow"
    Then the workspace collaborators list contains
      | Alice     | account |
      | Bob       | account |
      | Marketing | team    |
    When "Bob" fetches the accounts for workspace "SmartSparrow"
    Then the workspace collaborators list contains
      | Alice     | account |
      | Bob       | account |
      | Marketing | team    |

    Scenario: User should be able to limit a list of workspace accounts
      Given "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "SmartSparrow"
      When "Alice" fetches the accounts for workspace "SmartSparrow" with limit 1
      Then the workspace collaborators list contains
        | Alice      | account |
