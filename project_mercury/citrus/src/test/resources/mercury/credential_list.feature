Feature: Tests that fetch the credential details for given email

  Scenario: It should fetch credential details for an email
    Given an account "Alice" is created
    When "Alice" fetches credential type for an email
    Then credential type fetched successfully
