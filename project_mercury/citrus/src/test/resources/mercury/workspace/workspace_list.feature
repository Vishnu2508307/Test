Feature: Listing workspaces

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "one"
    And "Alice" has created workspace "two"

  Scenario: A workspace user should be able to list the workspaces he/she is collaborating to
    Given "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Bob" lists the workspaces
    Then the following workspaces are returned
      | one |

  Scenario: A workspace user should see an empty list if is not collaborating to any workspace
    When "Bob" lists the workspaces
    Then an empty list is returned

  Scenario: A workspace user should see a list of created workspaces
    When "Alice" lists the workspaces
    Then the following workspaces are returned
      | one |
      | two |

  Scenario: A user should see a list of workspaces shared with him and his teams
    Given "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has created a "Marketing" team
    And "Alice" adds "Bob" to the "Marketing" team as "CONTRIBUTOR"
    And "Alice" has granted "CONTRIBUTOR" permission level to "team" "Marketing" over workspace "two"
    When "Bob" lists the workspaces
    Then the following workspaces are returned
      | one |
      | two |
