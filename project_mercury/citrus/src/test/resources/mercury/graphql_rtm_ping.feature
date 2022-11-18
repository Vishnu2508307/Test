Feature: Testing GraphQL Query RTM

  Background:
    Given an account "Alice" is created
    And "Alice" is logged in to the default client

  Scenario: Request a GraphQL ping
    When "Alice" sends a GraphQL ping message
    Then mercury should respond with a GraphQL pong response
