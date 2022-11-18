Feature: Fetch an account information

  Background:
    Given A message type me.get
    And a user with email "citrus@dev.dev" is created once

  Scenario: Fetching an account information fails if user is not logged in
    When I post the message to mercury
    Then mercury should respond with: "me.get.error"

  Scenario: Fetching an account information successfully
    Given a user with email "citrus@dev.dev" and password "password" is logged in
    When I post the message to mercury
    Then mercury should respond with account details