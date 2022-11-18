Feature: Generate a developer key

  Background:
    Given A message type iam.developerKey.create
    And a user with email "citrus@dev.dev" is created once
    # {"type":"iam.developerKey.create"}

  Scenario: Developer Key should not be generated if user is not authenticated
    When I post the message to Mercury
    Then mercury should respond with: "iam.developerKey.create.error"
    #{"type":"iam.developerKey.create.error","message":"Unauthorized"}

  Scenario: Developer Key should not be generated if user is not an instructor
    Given a student is logged in
    When I post the message to Mercury
    Then mercury should respond with: "iam.developerKey.create.error"
    #{"type":"iam.developerKey.create.error","message":"Unauthorized"}

  Scenario: Developer Key should be generated
    Given a user with email "citrus@dev.dev" and password "password" is logged in
    When I post the message to Mercury
    Then mercury should send generated dev key
    #{"type":"iam.developerKey.create.ok","response":{"devkey":"oQCjF0b8QjGy1s41x9uMij6CFIrep3bo"}}