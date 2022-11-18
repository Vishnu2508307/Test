Feature: Cohort listing

  Background:
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created workspace "one"

  Scenario: The user is able to list all cohorts created by him
    Given "Alice" has created 20 cohorts for workspace "one"
    When "Alice" fetches a list of cohorts
    Then cohort list contains 20 cohorts

  Scenario: The user is able to list all cohorts shared with him or his teams
    Given "Alice" has created a cohort "Cohort1" for workspace "one"
    And "Alice" has granted "REVIEWER" permission to "Bob" over the cohort
    When "Bob" fetches a list of cohorts
    Then cohort list contains
      | Cohort1 |
    When "Alice" has created a "Engineering" team
    And "Alice" adds "Bob" to the "Engineering" team as "CONTRIBUTOR"
    Given "Alice" has created a cohort "Cohort2" for workspace "one"
    And "Alice" has granted "REVIEWER" permission to the "Engineering" team over the cohort
    When "Bob" fetches a list of cohorts
    Then cohort list contains
      | Cohort1 |
      | Cohort2 |

  Scenario: The user is able to list all cohorts shared with him as CONTRIBUTOR
    Given "Alice" has created a cohort for workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission to "Bob" over the cohort
    When "Bob" fetches a list of cohorts
    Then cohort list contains the created cohort

  Scenario: A user should be able to query the productId for a cohort
    Given a workspace account "Alice" is created
    When "Alice" creates a cohort in workspace "one"
    And the cohort is successfully created
    Then "Alice" can access the "productId" field "A103000103955" from the "cohort" cohort

  Scenario: A user should be able to query for a cohort without productId
    Given a workspace account "Alice" is created
    When "Alice" creates a cohort in workspace "one" without productId
    And the cohort is successfully created
    Then "Alice" can access the "productId" field '' from the "cohort" cohort
