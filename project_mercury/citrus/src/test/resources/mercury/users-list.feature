Feature: Fetching a list of users of an subscription

  Scenario: Got an error on listing users if not authenticated
    Given a user is not logged in
    When user requests a list of users
    Then mercury should respond with: "iam.subscription.user.list.error" and code "401"

  Scenario: Got an error on listing users if no workspace role
    Given a student is logged in
    When user requests a list of users
    Then mercury should respond with: "iam.subscription.user.list.error" and code "401"

  Scenario: Fetch list of all members with workspace roles
    Given a workspace account "User1" is created
    When "User1" creates account "User2" with workspace role
    And "User1" creates account "User3" with workspace role
    Then "User1" should fetch list of users
      | User1 |
      | User2 |
      | User3 |

  Scenario: Fetch only users with workspace roles
    Given a workspace account "User1" is created
    When "User1" creates account "User2" with student role
    Then "User1" should fetch list of users
      | User1 |

  Scenario: Fetch only users from the same subscription
    Given a workspace account "User1" is created
    When "User2" is created under different subscription
    Then "User1" should fetch list of users
      | User1 |

  Scenario: A user with no permission over the subscription should not be able to list the users
    Given a workspace account "Alice" is created
    And "Alice" creates account "Bob" with workspace role
    When "Bob" fetches the list of users
    Then the list of users is not returned due to missing permission level

  Scenario: A user with reviewer permission level should be able to list the users
    Given a workspace account "Alice" is created
    And "Alice" creates account "Bob" with workspace role
    Given "Alice" has granted "REVIEWER" permission level over the subscription to "account"
      | Bob |
    Then "Bob" should fetch list of users
      | Alice |
      | Bob   |

