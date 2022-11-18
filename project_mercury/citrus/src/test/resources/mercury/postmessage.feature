Feature: Post a message

  Scenario: Submit an invalid message
    Given A message type invalid
    When I post the message to mercury
    Then mercury should respond with: "error"

  Scenario: Submit a ping message
    Given A message type ping
    When I post the message to mercury
    Then mercury should respond with: "pong"

  Scenario: Submit a message with invalid type should return the replyTo field
    Given A message type unknown
    And message contains "id" "1234"
    When I post the message to mercury
    Then mercury should respond with an error and id "1234"