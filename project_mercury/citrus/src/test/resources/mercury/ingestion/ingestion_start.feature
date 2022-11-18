Feature: Start an ingestion

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created an ingestion request in project "TRO" in workspace "one"

  Scenario: A user without project owner permissions on the project cannot start an ingestion request
    Given an account "Bob" is created
    When "Bob" tries starting an ingestion request in project "TRO" with adapter type "EPUB"
    Then the ingestion is not started due to missing permission level

  Scenario: A user without project owner permissions on the project cannot start an ingestion request
    Given an account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" tries starting an ingestion request in project "TRO" with adapter type "EPUB"
    Then the ingestion is not started due to missing permission level

#  There is no client configured to send http requests to the mercury REST endpoints under /sns/
  @ignore
  Scenario: A user with project owner permissions on the project can start an ingestion request with status UPLOADED
    When "Alice" tries starting an ingestion request in project "TRO" with adapter type "EPUB"
    Then the ingestion request is successfully started

  Scenario: A user cannot start an ingestion request with invalid adapter type
    When "Alice" tries starting an ingestion request in project "TRO" with adapter type "PPT"
    Then the ingestion is not started due to invalid adapter type
