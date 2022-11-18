Feature: Granting and revoking permissions on a theme

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" created theme "theme_one" for workspace "one"

  Scenario: User with contributor or higher permission level over the theme should be able to grant permission
    Given "Alice" grants "Bob" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then "Bob" has "CONTRIBUTOR" permission level for theme "theme_one"

  Scenario: A user with reviewer or no permission level over the theme should not be able to grant permission
    Given "Alice" grants "Bob" with "REVIEWER" permission level for theme "theme_one"
    Then "Bob" has "REVIEWER" permission level for theme "theme_one"
    When "Bob" grants "Charlie" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then the theme is not shared due to missing permission level

  Scenario: A user should not be able to grant a permission that is higher than his/her own
    Given "Alice" grants "Bob" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then "Bob" has "CONTRIBUTOR" permission level for theme "theme_one"
    When "Bob" grants "Charlie" with "OWNER" permission level for theme "theme_one"
    Then the theme is not shared due to missing permission level

  Scenario: A user with contributor permission level should be able to grant permission to a list of accounts
    Given "Alice" grants "REVIEWER" permission level for theme "theme_one" to "account"
      | Bob     |
      | Charlie |
    Then the following "account" have "REVIEWER" permission level over theme "theme_one"
      | Bob     |
      | Charlie |

  Scenario: A user with contributor permission level should be able to grant permission to a list of teams
    Given "Alice" has created a "Marketing" team
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level for theme "theme_one" to "team"
      | Marketing |
      | Sales     |
    Then the following "team" have "REVIEWER" permission level over theme "theme_one"
      | Marketing |
      | Sales     |

  Scenario: It should not allow to override an existing higher permission level when the granter has a lower permission
    Given "Alice" grants "Bob" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then "Bob" has "CONTRIBUTOR" permission level for theme "theme_one"
    When "Bob" grants "Alice" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then the theme is not shared due to missing permission level

    ### Revoke scenarios

  Scenario: A user with contributor or higher permission level should be able to revoke an account with equal or lower permission
    Given "Alice" grants "Bob" with "OWNER" permission level for theme "theme_one"
    Then "Bob" has "OWNER" permission level for theme "theme_one"
    And "Alice" grants "Charlie" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then "Charlie" has "CONTRIBUTOR" permission level for theme "theme_one"
    When "Bob" revokes "Charlie"'s permission for theme "theme_one"
    Then the theme permission is successfully revoked

  Scenario: A user with contributor permission level should not be able to revoke an account with higher permission
    Given "Alice" grants "Bob" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then "Bob" has "CONTRIBUTOR" permission level for theme "theme_one"
    And "Alice" grants "Charlie" with "OWNER" permission level for theme "theme_one"
    Then "Charlie" has "OWNER" permission level for theme "theme_one"
    When "Bob" revokes "Charlie"'s permission for theme "theme_one"
    Then the request is denied due to missing permission level for theme

  Scenario: A user with reviewer permission level should not be able to revoke any permission
    Given "Alice" grants "Bob" with "REVIEWER" permission level for theme "theme_one"
    Then "Bob" has "REVIEWER" permission level for theme "theme_one"
    And "Alice" grants "Charlie" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then "Charlie" has "CONTRIBUTOR" permission level for theme "theme_one"
    When "Bob" revokes "Charlie"'s permission for theme "theme_one"
    Then the request is denied due to missing permission level for theme

  Scenario: A user with contributor permission level should be able to revoke team permission
    Given "Alice" has created a "Marketing" team
    And "Alice" has created a "Sales" team
    Given "Alice" grants "Bob" with "CONTRIBUTOR" permission level for theme "theme_one"
    Then "Bob" has "CONTRIBUTOR" permission level for theme "theme_one"
    And "Bob" grants "REVIEWER" permission level for theme "theme_one" to "team"
      | Marketing |
      | Sales     |
    Then the following "team" have "REVIEWER" permission level over theme "theme_one"
      | Marketing |
      | Sales     |
    When "Bob" revokes team "Marketing" permission for theme "theme_one"
    Then the theme permission is successfully revoked

  Scenario: A user with owner permission level should be able to revoke team permission
    Given "Alice" has created a "Marketing" team
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level for theme "theme_one" to team "Marketing"
    Then the team "Marketing" have "REVIEWER" permission level over theme "theme_one"
    When "Alice" revokes team "Marketing" permission for theme "theme_one"
    Then the theme permission is successfully revoked
