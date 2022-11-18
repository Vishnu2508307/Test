Feature: a user with owner permission level should be able to archive and un-archive a cohort

  Background:
    Given workspace accounts are created
      | Alice    |
      | Bob      |
      | Charlie  |
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort for workspace "one"
    And "Alice" has shared the created cohort with "Bob" as "REVIEWER"
    And "Alice" has shared the created cohort with "Charlie" as "CONTRIBUTOR"

  Scenario: an owner should be able to archive a cohort
    When "Alice" archives the created cohort
    Then the created cohort is archived

  Scenario: a reviewer should not be able to archive a cohort
    When "Bob" archives the created cohort
    Then the "archive" request is not authorized

  Scenario: a contributor should not be able to archive a cohort
    When "Charlie" archives the created cohort
    Then the "archive" request is not authorized

  Scenario: an owner should be able to unarchive a cohort
    Given "Alice" has archived the created cohort
    When "Alice" un-archives the created cohort
    Then the created cohort is un-archived

  Scenario: a reviewer should not be able to un-archive a cohort
    Given "Alice" has archived the created cohort
    When "Bob" un-archives the created cohort
    Then the "unarchive" request is not authorized

  Scenario: a contributor should not be able to un-archive a cohort
    Given "Alice" has archived the created cohort
    When "Charlie" un-archives the created cohort
    Then the "unarchive" request is not authorized
