Feature: Granting permissions on subscription

  Scenario: User can't share subscription without permissions
    Given a workspace account "Admin1" is created
    And "Admin1" creates accounts with workspace role
      |User1|
      |User2|
    When "User1" shares his subscription with "User2" as "CONTRIBUTOR"
    Then mercury should respond with: "iam.subscription.permission.grant.error" and code "401"

  Scenario: User shares the subscription successfully
    Given a workspace account "Admin1" is created
    And "Admin1" creates accounts with workspace role
      |User1|
      |User2|
    When "Admin1" shares his subscription with "User1" as "CONTRIBUTOR"
    Then subscription shared successfully
    And "User1" can share his subscription with "User2" as "CONTRIBUTOR"

  Scenario: A workspace user should not be able to grant a permission that is higher than his/her own
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Alice" has shared his subscription with "Bob" as "CONTRIBUTOR"
    When "Bob" shares "Alice"'s subscription with "Charlie" as "OWNER"
    Then mercury should respond with: "iam.subscription.permission.grant.error" and code "401"

  Scenario: A workspace user with contributor permission level should be able to grant permission to a list of accounts
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    When "Alice" grants "REVIEWER" permission level over the subscription to "account"
      | Bob |
      | Charlie |
    Then the following "account" have "REVIEWER" permission level over "Alice"'s subscription
      | Bob |
      | Charlie |

  Scenario: A workspace user with contributor permission level should be able to grant permission to a list of teams
    Given a workspace account "Alice" is created
    And "Alice" has created a "Marketing" team
    And "Alice" has created a "Sales" team
    When "Alice" grants "REVIEWER" permission level over the subscription to "team"
      | Marketing |
      | Sales |
    Then the following "team" have "REVIEWER" permission level over "Alice"'s subscription
      | Marketing |
      | Sales |

  Scenario: It should not allow to override an existing higher permission with a lower permission
    Given a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Bob" has shared his subscription with "Charlie" as "CONTRIBUTOR"
    When "Charlie" shares "Bob"'s subscription with "Bob" as "CONTRIBUTOR"
    Then mercury should respond with: "iam.subscription.permission.grant.error" and code "401"

  Scenario: It should allow to override an existing higher permission with a lower when the requesting user has higher permission level
    Given a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Bob" has shared his subscription with "Charlie" as "OWNER"
    When "Charlie" shares "Bob"'s subscription with "Bob" as "CONTRIBUTOR"
    Then subscription shared successfully
