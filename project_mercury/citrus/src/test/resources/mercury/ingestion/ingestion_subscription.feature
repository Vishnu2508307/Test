Feature: Subscriptions for ingestion

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created an ingestion request in project "TRO" in workspace "one"

  Scenario: A ingestion user can subscribe/unsubscribe to project ingestion with owner permission
    When "Alice" subscribes to the ingestion for project "TRO"
    Then "Alice" is successfully subscribed to the ingestion
    When "Alice" unsubscribes to the ingestion for project "TRO"
    Then "Alice" is successfully unsubscribed to the ingestion

  Scenario: A ingestion user can subscribe/unsubscribe to project ingestion with owner permission
    When "Alice" subscribes to the ingestion for project "TRO"
    Then "Alice" is successfully subscribed to the ingestion
    When "Alice" unsubscribes to the ingestion for project "TRO"
    Then "Alice" is successfully unsubscribed to the ingestion

  Scenario: A workspace user can not subscribe/unsubscribe to project ingestion without permission
    Given an account "Bob" is created
    When "Bob" subscribes to the ingestion for project "TRO"
    Then mercury should respond with: "project.ingest.subscribe.error" and code "401"
    When "Bob" unsubscribes to the ingestion for project "TRO"
    Then mercury should respond with: "project.ingest.unsubscribe.error" and code "401"