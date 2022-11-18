Feature: Fetch an ingestion

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: A user without project owner permissions on the project cannot get an ingestion request
    Given an account "Bob" is created
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one"
    Then the ingestion request is successfully created
    When "Bob" tries to fetch the ingestion request in project "TRO"
    Then the ingestion is not fetched due to missing permission level

  Scenario: A user with project owner permissions on the project can fetch an ingestion request
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one"
    Then the ingestion request is successfully created
    When "Alice" tries to fetch the ingestion request in project "TRO"
    Then the ingestion request is successfully fetched

  Scenario: A user with project owner permissions on the project can fetch an ingestion request
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one"
    Then the ingestion request is successfully created
    When "Alice" tries to fetch the ingestion request in project "TRO"
    Then the ingestion request is successfully fetched