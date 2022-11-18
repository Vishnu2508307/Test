Feature: Tests that fetching the account information by email is not possible by a non support role user

  Scenario: It should not allow a user to fetch another user info when they do not have the support role
    Given an account "Alice" is created
    And an account "Bob" is created
    When "Alice" fetches account info using "Bob"'s email
    Then the account by email info request is not allowed

  Scenario: It should allow a support role user to fetch another user info
    Given an account "Alice" is created
    And a support role account "Bob" exists
    When "Bob" fetches account info using "Alice"'s email
    Then "Alice"'s account information is successfully fetched
