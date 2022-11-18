Feature: Citrus should allow multiple concurrent client connections

  Background:
    Given an account "Bob" is created
    And an account "Tom" is created

  Scenario: Two users authenticated
    Given "Bob" is logged in to the default client
    And "Tom" is logged via a "second" client
    Then the default client returns "Bob" account details
    And the "second" client returns "Tom" account details
