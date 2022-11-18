Feature: Granting and revoking permissions on a team

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    Given "Alice" has created a "Marketing" team

  Scenario: A workspace user that owns a team should be able to grant/revoke team permissions
    When "Alice" adds "Bob" to the team "Marketing" as "REVIEWER"
    Then "Bob" is successfully added to the team "Marketing" as "REVIEWER"
    When "Alice" revokes "Bob"'s "Marketing" permission
    Then the team permission is successfully revoked

  Scenario: A workspace user with contributor or higher permission level over the team should be able to grant
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission over the team "Marketing"
    When "Bob" adds "Charlie" to the team "Marketing" as "CONTRIBUTOR"
    Then "Charlie" is successfully added to the team "Marketing" as "CONTRIBUTOR"

  Scenario: A workspace user with reviewer permission level over the team should not be able to grant
    Given "Alice" has granted "Bob" with "REVIEWER" permission over the team "Marketing"
    When "Bob" adds "Charlie" to the team "Marketing" as "REVIEWER"
    Then the team permission is not "grant"ed

  Scenario: A workspace user with contributor permission should not be able to grant a level higher than his/her own
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission over the team "Marketing"
    When "Bob" adds "Charlie" to the team "Marketing" as "OWNER"
    Then the team permission is not "grant"ed

  Scenario: A workspace user with contributor or higher permission level should be able to revoke
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission over the team "Marketing"
    And "Alice" has granted "Charlie" with "CONTRIBUTOR" permission over the team "Marketing"
    When "Charlie" revokes "Bob"'s "Marketing" permission
    Then the team permission is successfully revoked

  Scenario: A workspace user with contributor permission should not be able to revoke a permission higher than his/her own
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission over the team "Marketing"
    And "Alice" has granted "Charlie" with "OWNER" permission over the team "Marketing"
    When "Bob" revokes "Charlie"'s "Marketing" permission
    Then the team permission is not "revoke"ed

  Scenario: A workspace user with reviewer permission should not be able to revoke a permission
    Given "Alice" has granted "Bob" with "REVIEWER" permission over the team "Marketing"
    And "Alice" has granted "Charlie" with "REVIEWER" permission over the team "Marketing"
    When "Bob" revokes "Charlie"'s "Marketing" permission
    Then the team permission is not "revoke"ed

  Scenario: It should not override an existing permission that is higher than the requester permission
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission over the team "Marketing"
    When "Bob" adds "Alice" to the team "Marketing" as "REVIEWER"
    Then the team permission is not "grant"ed

  Scenario: It should allow to override an existing permission when the requester permission is equal or higher
    Given "Alice" has granted "Bob" with "OWNER" permission over the team "Marketing"
    When "Bob" adds "Alice" to the team "Marketing" as "CONTRIBUTOR"
    Then "Alice" is successfully added to the team "Marketing" as "CONTRIBUTOR"
