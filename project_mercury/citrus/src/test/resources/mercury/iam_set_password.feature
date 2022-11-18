Feature: Set account password

  Background:
    Given a support role account "Alice" exists
    And an account "Bob" is created
    And an account "Charlie" is created

  Scenario: A non-support role user should not be able to set the password for another account
    When "Charlie" sets the password for account "Bob" as "newPassword"
    Then the account password is not set due to missing permission level

  Scenario: A support role user should be able to set the password for another account
    When "Alice" sets the password for account "Bob" as "newPassword"
    Then the account password is successfully set
    Then "Bob" can authenticate using password "newPassword"

  Scenario: A user should be able to set the password for their account
    When "Bob" sets the password for their account as "newPassword"
    Then the password is successfully set
    Then "Bob" can authenticate using password "newPassword"