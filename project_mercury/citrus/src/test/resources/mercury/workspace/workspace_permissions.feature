Feature: Granting and revoking permissions on a workspace

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Alice" has created workspace "one"

  Scenario: A workspace user with contributor or higher permission level over the workspace should be able to grant permission
    When "Alice" grants "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    Then "Bob" has "CONTRIBUTOR" permission level on workspace "one"

  Scenario: A workspace user with reviewer or no permission level over the workspace should not be able to grant permission
    Given "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Bob" grants "Charlie" with "CONTRIBUTOR" permission level on workspace "one"
    Then the workspace is not shared due to missing permission level

  Scenario: A workspace user should not be able to grant a permission that is higher than his/her own
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" grants "Charlie" with "OWNER" permission level on workspace "one"
    Then the workspace is not shared due to missing permission level

  Scenario: A workspace user with contributor or higher permission level should be able to revoke an account with equal or lower permission
    Given "Alice" has granted "Bob" with "OWNER" permission level on workspace "one"
    And "Alice" has granted "Charlie" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" revokes "Charlie"'s permission on workspace "one"
    Then the workspace permission is successfully revoked

  Scenario: A workspace user with contributor permission level should not be able to revoke an account with higher permission
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "Charlie" with "OWNER" permission level on workspace "one"
    When "Bob" revokes "Charlie"'s permission on workspace "one"
    Then the request is denied due to missing permission level

  Scenario: A workspace user with reviewer permission level should not be able to revoke any permission
    Given "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "Charlie" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" revokes "Charlie"'s permission on workspace "one"
    Then the request is denied due to missing permission level

  Scenario: A workspace user with contributor permission level should be able to grant permission to a list of accounts
    Given "Alice" grants "REVIEWER" permission level on workspace "one" to "account"
    | Bob |
    | Charlie |
  Then the following "account" have "REVIEWER" permission level over workspace "one"
    | Bob |
    | Charlie |

  Scenario: A workspace user with contributor permission level should be able to grant permission to a list of teams
    Given "Alice" has created a "Marketing" team
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level on workspace "one" to "team"
      | Marketing |
      | Sales |
    Then the following "team" have "REVIEWER" permission level over workspace "one"
      | Marketing |
      | Sales |

  Scenario: A workspace user with contributor permission level should be able to grant permission to a list of teams
    Given "Alice" has created a "Marketing" team
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level on workspace "one" to "team"
      | Marketing |
      | Sales |
    Then the following "team" have "REVIEWER" permission level over workspace "one"
      | Marketing |
      | Sales |
    When "Alice" revokes team permission on workspace "one" for "Marketing" team
    Then the workspace permission is successfully revoked

  Scenario: It should not allow to override an existing higher permission level when the granter has a lower permission
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" grants "Alice" with "CONTRIBUTOR" permission level on workspace "one"
    Then the workspace is not shared due to missing permission level

  Scenario: It should allow to override an existing higher permission when the granter has equal or higher permission
    Given "Alice" has granted "Bob" with "OWNER" permission level on workspace "one"
    When "Bob" grants "Alice" with "CONTRIBUTOR" permission level on workspace "one"
    Then "Alice" has "CONTRIBUTOR" permission level on workspace "one"
