Feature: Account provision event

  Background:
    Given a user with email "citrus@dev.dev" is created once
    And a list of workspace users
      | Alice |
      | Bob   |
    And "Alice" is logged in to the default client
    And "Bob" is logged via a "bob" client

  Scenario: Verify subscribe to event
    Given "Bob" subscribes to "account provision" events via a "bob" client
    When "Alice" provisions a new user "Chuck" within the same subscription with roles
     | STUDENT |
    Then "Bob" should receive an account provisioned "ACCOUNT_PROVISIONED" notification via a "bob" client
