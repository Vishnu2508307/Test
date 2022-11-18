Feature: Generate a LTI consumer key: negative scenarios
#    positive scenario is tested in lti.feature

  Background:
    Given A message type iam.ltiConsumerKey.create
    And a user with email "citrus@dev.dev" is created once

  Scenario: LTI Consumer Key should not be generated if user is not authenticated
    When I post the message to Mercury
    Then mercury should respond with: "iam.ltiConsumerKey.create.error" and code "401"

  Scenario: LTI Consumer Key should not be generated if user is not an instructor
    Given a student is logged in
    When I post the message to Mercury
    Then mercury should respond with: "iam.ltiConsumerKey.create.error" and code "401"
