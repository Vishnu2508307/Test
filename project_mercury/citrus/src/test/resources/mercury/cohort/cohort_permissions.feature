Feature: Granting and revoking cohort permissions for account

  Background:
    Given workspace accounts are created
      | Alice   |
      | Bob     |
      | Charlie |
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort for workspace "one"

  Scenario: The cohort owner should be able to grant permissions to a cohort for multiple users
    Given "Alice" grants "REVIEWER" permission over the cohort to the users
      | Bob     |
      | Charlie |
    Then cohort "REVIEWER" permissions successfully given to
      | Bob     |
      | Charlie |
    And "Bob" can fetch the cohort
    And "Charlie" can fetch the cohort

  Scenario: A cohort contributor should be able to grant permissions to a cohort
    Given "Alice" has granted "CONTRIBUTOR" permission to "Bob" over the cohort
    When "Bob" grants "REVIEWER" permission to "Charlie" over the cohort
    Then "Charlie" has "REVIEWER" permission over the cohort

  Scenario: A cohort reviewer is not authorized to grant permission to a cohort
    Given "Alice" has granted "REVIEWER" permission to "Bob" over the cohort
    When "Bob" grants "REVIEWER" permission to "Charlie" over the cohort
    Then the permission is not granted

  Scenario: A workspace user should not be able to grant a permission level higher than his/her own
    Given "Alice" has granted "CONTRIBUTOR" permission to "Bob" over the cohort
    When "Bob" grants "OWNER" permission to "Charlie" over the cohort
    Then the permission is not granted

  Scenario: A cohort owner should be able to revoke permission from a cohort
    Given "Alice" has granted "REVIEWER" permission to "Bob" over the cohort
    When "Alice" revokes "Bob"'s permission
    Then the permission is successfully revoked

  Scenario: An equal permission level should be able to revoke permission from a cohort
    Given "Alice" has granted "CONTRIBUTOR" permission to "Bob" over the cohort
    And "Alice" has granted "CONTRIBUTOR" permission to "Charlie" over the cohort
    When "Bob" revokes "Charlie"'s permission
    Then the permission is successfully revoked

  Scenario: An higher permission level should be able to revoke permission from a cohort
    Given "Alice" has granted "CONTRIBUTOR" permission to "Bob" over the cohort
    And "Alice" has granted "REVIEWER" permission to "Charlie" over the cohort
    When "Bob" revokes "Charlie"'s permission
    Then the permission is successfully revoked

  Scenario: A lower permission level should not be able to revoke permission from a cohort
    Given "Alice" has granted "CONTRIBUTOR" permission to "Bob" over the cohort
    And "Alice" has granted "OWNER" permission to "Charlie" over the cohort
    When "Bob" revokes "Charlie"'s permission
    Then the permission is not revoked

  Scenario: It should not allow to override higher existing permission level when granting
    Given "Alice" has granted "CONTRIBUTOR" permission to "Bob" over the cohort
    When "Bob" grants "REVIEWER" permission to "Alice" over the cohort
    Then the permission is not granted

  Scenario: It should allow to override to a lower permission level when the user has equal or higher permission
    Given "Alice" has granted "OWNER" permission to "Bob" over the cohort
    When "Bob" grants "REVIEWER" permission to "Alice" over the cohort
    Then "Alice" has "REVIEWER" permission over the cohort
