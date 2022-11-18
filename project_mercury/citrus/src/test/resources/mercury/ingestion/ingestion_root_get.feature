Feature: Fetch an ingestion by root element id

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"

  Scenario: A user without project owner permissions on the project cannot get an ingestion request by root element
    Given an account "Bob" is created
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one" with root element "UNIT_ONE"
    Then the ingestion request is successfully created
    When "Bob" tries to fetch the ingestion request in project "TRO" by root element "UNIT_ONE"
    Then the ingestion by root element is not fetched due to missing permission level

  Scenario: A user with project owner permissions on the project can fetch an ingestion request by root element
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one" with root element "UNIT_ONE"
    Then the ingestion request is successfully created
    When "Alice" tries to fetch the ingestion request in project "TRO" by root element "UNIT_ONE"
    Then the ingestion request is successfully fetched by root element "UNIT_ONE"

  Scenario: A user with project owner permissions on the project can fetch an ingestion request
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one" with root element "UNIT_ONE"
    Then the ingestion request is successfully created
    When "Alice" tries to fetch the ingestion request in project "TRO" by root element "UNIT_ONE"
    Then the ingestion request is successfully fetched by root element "UNIT_ONE"