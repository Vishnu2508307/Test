Feature: List of deployments for a cohort

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has saved configuration for "UNIT_ONE" activity
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: User should be able to see list of deployments for the cohort
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_TWO"
    When "Alice" lists deployments for the cohort
    Then deployment list contains
      | DEPLOYMENT_ONE | UNIT_ONE |
      | DEPLOYMENT_TWO | UNIT_ONE |

  Scenario: User should not be able to see cohort deployments if cohort is not shared with him
    Given a workspace account "Bob" is created
    When "Bob" lists deployments for the cohort
    Then lists of deployments fails with code 401 and message "Unauthorized: Unauthorized permission level"
    When "Alice" has granted "REVIEWER" permission to "Bob" over the cohort
    And "Bob" lists deployments for the cohort
    Then deployment list contains
      | DEPLOYMENT_ONE | UNIT_ONE |
