Feature: Authenticate a user

  Background:
    Given A message type authenticate
    And a user with email "citrus@dev.dev" is created once

  Scenario: Authentication with credentials fails
    Given message contains "email" "invalid@email.dev"
    And message contains "password" "incorrect"
    When I post the message to Mercury
    Then It should "not authenticate" the user

  Scenario: Authentication with credentials succeeds
    Given message contains "email" "citrus@dev.dev"
    And message contains "password" "password"
    When I post the message to Mercury
    Then It should "authenticate" the user

  Scenario: Authentication with bearer token fails
    Given message contains "bearerToken" "invalidToken"
    When I post the message to Mercury
    Then It should "not authenticate" the user

  Scenario: Authentication with bearer token succeeds
    Given a user is logged in and "bearerToken" is stored
    And a message with "bearerToken" for "bearerToken"
    When I post the message to Mercury
    Then It should "authenticate" the user