Feature: Logout a user

  Background:
    Given A message type me.logout
    And a user with email "citrus@dev.dev" is created once

  Scenario: Logout fails if user is not authenticated
    Given message contains "bearerToken" "anyToken"
    When I post the message to Mercury
    Then mercury should respond with: "me.logout.error" and code "401"

  Scenario: Logout fails if invalid bearer token is provided
    Given a user with email "citrus@dev.dev" and password "password" is logged in
    Given message contains "bearerToken" "invalidToken"
    When I post the message to Mercury
    Then mercury should respond with: "me.logout.error" and code "400"

  Scenario: Successful logout
    Given a user is logged in and "bearerToken" is stored
    And a message with "bearerToken" for "bearerToken"
    When I post the message to Mercury
    Then mercury should respond with: "me.logout.ok"

    When A message type me.get
    And I post the message to mercury
    Then mercury should respond with: "me.get.error" and code "401"

  Scenario: Logout fails if old bearer token is used
    Given a workspace account "Alice" is created
    And "Alice" is authenticated and bearer token saved
    And "Alice" is logged out
    And "Alice" is authenticated
    Then "Alice" can not logout with old bearer token due to code 400 and message "Unable to logout: supplied bearer token is not valid"
