Feature: Listing cohort enrollments

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And an ies account "Faith" is provisioned
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort for workspace "one"
    And "Faith" autoenroll to cohort "cohort"

  Scenario: The cohort owner should be able to list the enrollments for a cohort
    When "Alice" lists the enrollments for the created cohort
    Then "Faith" shows up in the list of enrolled users

  Scenario: a cohort contributor should be able to list the enrollments
    Given "Alice" has shared the created cohort with "Bob" as "CONTRIBUTOR"
    When "Bob" lists the enrollments for the created cohort
    Then "Faith" shows up in the list of enrolled users

  Scenario: a cohort reviewer should be able to list the enrollments for a cohort
    Given "Alice" has shared the created cohort with "Bob" as "REVIEWER"
    When "Bob" lists the enrollments for the created cohort
    Then "Faith" shows up in the list of enrolled users

  Scenario: a user with no permission should not be able to view the enrollments
    When "Charlie" lists the enrollments for the created cohort
    Then the request is not authorized
