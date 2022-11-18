Feature: Test rtm messages for creating, deleting and fetching a manual grading configuration for a component

  Background:
    Given an account "Arthur" is created
    And an account "Betty" is created
    And "Arthur" has created and published course plugin
    And "Arthur" has created workspace "WORKSPACE_ONE"
    And "Arthur" has created and published course plugin
    And "Arthur" has created project "TRO" in workspace "WORKSPACE_ONE"
    And "Arthur" has created activity "COURSE_ONE" inside project "TRO"
    And "Arthur" has created a "PATHWAY_ONE" pathway for the "COURSE_ONE" activity
    And "Arthur" has created a "INTERACTIVE_ONE" interactive for the "PATHWAY_ONE" pathway
    And "Arthur" has created a "COMPONENT_ONE" component for the "INTERACTIVE_ONE" interactive
    And "Arthur" has granted "REVIEWER" permission level to "account" "Betty" over workspace "WORKSPACE_ONE"
    And "Arthur" has granted "REVIEWER" permission level to "account" "Betty" over project "TRO"

  Scenario: It should not allow a user with invalid permission level to create a configuration
    When "Betty" set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 10 |
    Then the manual grading configuration is not "set" due to missing permission level

  Scenario: It should not allow a user with invalid permission level to delete a configuration
    Given "Arthur" has set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 10 |
    When "Betty" deletes "COMPONENT_ONE" manual grading configurations
    Then the manual grading configuration is not "delete" due to missing permission level

  Scenario: It should allow a user with proper permission level to create a configuration
    When "Arthur" set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 10 |
    Then the manual grading configuration for "COMPONENT_ONE" is set with
      | maxScore | 10 |

  Scenario: It should allow a user with proper permission level to delete a configuration
    Given "Arthur" has set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 10 |
    When "Arthur" deletes "COMPONENT_ONE" manual grading configurations
    Then the manual grading configuration is successfully deleted
    When "Arthur" fetches the manual grading configurations for "COMPONENT_ONE"
    Then the "COMPONENT_ONE" manual grading configuration is empty

  Scenario: It should allow a reviewer to fetch a manual grading configuration
    Given "Arthur" has set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 10 |
    When "Betty" fetches the manual grading configurations for "COMPONENT_ONE"
    Then the "COMPONENT_ONE" manual grading configuration is returned with
      | maxScore | 10 |

  Scenario: It should broadcast the payload to other clients when the configuration is created/deleted
    Given "Betty" is logged via a "ONE" client
    And "Betty" subscribes to "activity" events for "COURSE_ONE" via a "ONE" client
    When "Arthur" has set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 10 |
    Then "Betty" receives "COMPONENT_ONE" changes with action "MANUAL_GRADING_CONFIGURATION_CREATED" on client "ONE"
    Given "Arthur" has deleted "COMPONENT_ONE" manual grading configurations
    Then "Betty" receives "COMPONENT_ONE" changes with action "MANUAL_GRADING_CONFIGURATION_DELETED" on client "ONE"
