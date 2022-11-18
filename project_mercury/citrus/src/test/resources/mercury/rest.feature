@noSocket
Feature: Testing REST API

  Scenario: Request a ping resource
    When user requests "/ping" resource
    Then user receives "pong" response