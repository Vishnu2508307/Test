Feature: Delete an ingestion

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: A user without project owner permissions on the project delete create an ingestion request
    Given an account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one"
    Then the ingestion request is successfully created
    When "Bob" tries deleting an ingestion request in project "TRO"
    Then the ingestion is not deleted due to missing permission level

  Scenario: A user with project owner permissions on the project can create an ingestion request
    When "Alice" subscribes to the project "TRO"
    Then "Alice" is successfully subscribed to the project
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one"
    Then the ingestion request is successfully created
    And "Alice" gets an event broadcast as "UPLOADING" for project "TRO"
    When "Alice" tries deleting an ingestion request in project "TRO"
    Then the ingestion request is successfully deleted
    And "Alice" gets an event broadcast as "DELETED" for project "TRO"

  Scenario: A user with project owner permissions on the activity can create and delete an ingestion request
    And "Alice" has created activity "COURSE" inside project "TRO"
    Then "Alice" can subscribe to "COURSE" activity events successfully
    When "Alice" tries creating an ingestion request in activity "COURSE" in project "TRO" in workspace "one"
    Then the ingestion request is successfully created
    And "Alice" gets an event broadcast as "UPLOADING" for activity "COURSE"
    When "Alice" tries deleting an ingestion request in activity "COURSE"
    Then the ingestion request is successfully deleted
    And "Alice" gets an event broadcast as "DELETED" for activity "COURSE"

