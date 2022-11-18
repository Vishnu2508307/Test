Feature: Create a cohort

  Background:
    Given a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Bob" has created workspace "one"

  Scenario: The workspace OWNER should be able to create a cohort
    Given a workspace account "Bob" is created
    When "Bob" creates a cohort in workspace "one"
    Then the cohort is successfully created

  Scenario: The workspace CONTRIBUTOR should be able to create a cohort
    Given "Bob" has granted "Charlie" with "CONTRIBUTOR" permission level on workspace "one"
    When "Charlie" creates a cohort in workspace "one"
    Then the cohort is successfully created

  Scenario: The workspace REVIEWER should not be able to create a cohort
    Given "Bob" has granted "Charlie" with "REVIEWER" permission level on workspace "one"
    When "Charlie" creates a cohort in workspace "one"
    Then "Charlie" is not able to create the cohort due to missing permission level

  Scenario: The workspace OWNER should be able to create a cohort with LTI enrollment type
    Given a workspace account "Bob" is created
    When "Bob" creates a cohort in workspace with Lti type "one" with consumer credential
    """
       { "key": "lti_key", "secret": "lti_secret" }
    """
    Then the cohort with Lti type is successfully created

  Scenario: The workspace OWNER should be able to create a cohort
    Given a workspace account "Bob" is created
    When "Bob" creates a cohort in workspace "one" with invalid date
      | name             | Learner redirect test             |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GM     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | workspaceId      | ${one_workspace_id}               |
    Then the cohort is not created
