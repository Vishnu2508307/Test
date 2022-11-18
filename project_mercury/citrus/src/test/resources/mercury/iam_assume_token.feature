Feature: Can assume a token of another user

  Scenario: Assume to an empty account fails
    Given A message type iam.assume.token
    And message contains "accountId" ""
    When I post the message to Mercury
    Then mercury should respond with: "iam.assume.token.error"

  Scenario: Assume to an invalid account fails
    Given A message type iam.assume.token
    And message contains "accountId" "0ae82c4a-bf55-11e9-9cb5-2a2ae2dbcce4"
    When I post the message to Mercury
    Then mercury should respond with: "iam.assume.token.error"

  # Scenario: Assume token as a non-support SUPPORT role fails
  # Scenario: Assume token to a user with a SUPPORT role fails

