Feature: Delete a workspace

  Background:
    Given a workspace account "Alice" is created
    And "Alice" provides "Bob" with a new account in the subscription as
      | DEVELOPER  |
      | AERO_INSTRUCTOR |

  Scenario: A workspace user with contributor or higher permission level over the workspace should not be able to delete the workspace
    Given "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" tries deleting workspace "one"
    Then the workspace is not deleted due to missing permission level

  Scenario: A workspace user with reviewer or no permission level over the workspace should not be able to delete the workspace
    Given "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Bob" tries deleting workspace "one"
    Then the workspace is not deleted due to missing permission level

  Scenario: A workspace user with owner or higher permission level over the workspace should be able to delete the workspace
    Given "Alice" has created workspace "one"
    When "Alice" tries deleting workspace "one"
    Then workspace "one" is successfully deleted

  Scenario: A shared workspace should be deleted for all users
    Given "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Alice" lists the workspaces
    Then the following workspaces are returned
      | one |
    When "Bob" lists the workspaces
    Then the following workspaces are returned
      | one |
    When "Alice" tries deleting workspace "one"
    Then workspace "one" is successfully deleted
    When "Alice" lists the workspaces
    Then an empty list is returned
    When "Bob" lists the workspaces
    Then an empty list is returned

  Scenario: A shared workspace should be deleted for all users in a team
    Given "Alice" has created workspace "one"
    And "Alice" has created a "Marketing" team
    And "Alice" adds "Bob" to the "Marketing" team as "REVIEWER"
    And "Alice" grants "REVIEWER" permission level on workspace "one" to "team"
      | Marketing |
    Then the following "team" have "REVIEWER" permission level over workspace "one"
      | Marketing |
    When "Alice" lists the workspaces
    Then the following workspaces are returned
      | one |
    When "Bob" lists the workspaces
    Then the following workspaces are returned
      | one |
    When "Alice" tries deleting workspace "one"
    Then workspace "one" is successfully deleted
    When "Alice" lists the workspaces
    Then an empty list is returned
    When "Bob" lists the workspaces
    Then an empty list is returned
