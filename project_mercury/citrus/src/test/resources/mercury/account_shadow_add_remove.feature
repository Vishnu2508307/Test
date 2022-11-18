Feature: Add or remove shadow attribute for support account

  Background:
    Given an account "Alice" is created
    And a support role account "Bob" exists

  Scenario: Add shadow attribute to support account
    When "Bob" has added "INSTITUTION" shadow and value as "shadow"
    Then shadow attribute is successfully created to "Bob"

  Scenario: Add shadow attribute to non support account
    When "Alice" has added "INSTITUTION" shadow and value as "shadow"
    Then failed to add shadow attribute "Alice"

  Scenario: Delete shadow attribute for support account
    Given "Bob" has added "INSTITUTION" shadow and value as "shadow"
    Then shadow attribute is successfully created to "Bob"
    When "Bob" tries to delete shadow attribute as "INSTITUTION"
    Then shadow attribute is successfully removed to "Bob"

  Scenario: Delete shadow attribute to non support account
    When "Alice" tries to delete shadow attribute as "INSTITUTION"
    Then failed to remove shadow attribute "Alice"


