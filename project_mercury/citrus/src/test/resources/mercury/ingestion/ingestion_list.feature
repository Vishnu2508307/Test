Feature: List ingestion summaries for project

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: A user without project owner permissions on the project cannot list ingestion summaries
    Given an account "Bob" is created
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one"
    Then the ingestion request is successfully created
    When "Bob" tries to list all ingestion summaries in project "TRO"
    Then the ingestion summaries are not listed due to missing permission level

  Scenario: A user with project owner permissions on the project can list ingestion summaries
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one" with course name "Course 1"
    Then the ingestion request is successfully created
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one" with course name "Course 2"
    Then the ingestion request is successfully created
    When "Alice" tries to list all ingestion summaries in project "TRO"
    Then the following ingestion summaries are listed for project "TRO" with course names
      | Course 1 |
      | Course 2 |

  Scenario: A user with project owner or higher permissions on the project can list ingestion summaries
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one" with course name "Course 1"
    Then the ingestion request is successfully created
    When "Alice" tries creating an ingestion request in project "TRO" in workspace "one" with course name "Course 2"
    Then the ingestion request is successfully created
    When "Alice" tries to list all ingestion summaries in project "TRO"
    Then the following ingestion summaries are listed for project "TRO" with course names
      | Course 1 |
      | Course 2 |