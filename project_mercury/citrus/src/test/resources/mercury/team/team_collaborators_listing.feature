Feature: Listing the collaborators of a team

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And a workspace account "Debra" is created
    And "Alice" has created a "Marketing" team

  Scenario: A workspace user should be able to list the members of a team
    Given "Alice" adds "Bob" to the "Marketing" team as "REVIEWER"
    And "Alice" adds "Charlie" to the "Marketing" team as "CONTRIBUTOR"
    And "Alice" adds "Debra" to the "Marketing" team as "OWNER"
    When "Alice" lists the "Marketing" team collaborators
    Then the following collaborators are returned
    | Alice   |
    | Bob     |
    | Charlie |
    | Debra   |
    When "Debra" revokes "Bob"'s "Marketing" team permission
    And "Debra" lists the "Marketing" team collaborators
    Then the following collaborators are returned
      | Alice   |
      | Charlie |
      | Debra   |

  Scenario: A workspace user should be able to list the members of a team specifying a limit
    Given "Alice" adds "Bob" to the "Marketing" team as "REVIEWER"
    And "Alice" adds "Charlie" to the "Marketing" team as "CONTRIBUTOR"
    When "Alice" lists the "Marketing" team collaborators with limit 1
    Then only 1 team collaborator is returned with a total of 3
