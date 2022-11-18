Feature: Granting and revoking cohort permissions for teams

  Background:
    Given workspace accounts are created
      | Alice   |
      | Bob     |
      | Charlie |
    And "Alice" has created teams
      | Marketing   |
      | Engineering |
    And "Alice" adds "Bob" to the "Marketing" team as "CONTRIBUTOR"
    And "Alice" adds "Charlie" to the "Engineering" team as "CONTRIBUTOR"
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort for workspace "one"

  Scenario: The cohort owner should be able to grant permissions to a cohort for multiple teams and team members get the team permission level over the cohort.
    Given "Alice" has granted "REVIEWER" permission to the teams over the cohort
      | Marketing   |
      | Engineering |
    And "Bob" can fetch the cohort
    And "Charlie" can fetch the cohort

  Scenario: If user has both team and account permissions over the cohort, the highest permission level should win.
    Given "Alice" has granted "REVIEWER" permission to "Bob" over the cohort
    And "Alice" has granted "CONTRIBUTOR" permission to the "Marketing" team over the cohort
    When "Bob" updates this cohort
      | name           | Bob's cohort |
      | enrollmentType | PASSPORT     |
    Then the cohort is successfully updated
    When "Alice" has revoked "CONTRIBUTOR" permission from the "Marketing" team over the cohort
    When "Bob" updates this cohort
      | name           | Bob's cohort |
      | enrollmentType | PASSPORT     |
    Then "Bob" is not able to update the cohort due to missing permission level


