Feature: Get the ancestry of a courseware element

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "SCREEN_TWO" interactive

  Scenario: It should allow a workspace user to fetch the courseware type and ancestry
    When "Alice" fetches the courseware ancestry for "COMPONENT_ONE" in workspace "one"
    Then the courseware element type is "COMPONENT" and the ancestry has
      | SCREEN_TWO |
      | LINEAR_ONE |
      | UNIT_ONE   |

  Scenario: It should allow a learner to fetch a learner element type and ancestry
    Given "Alice" has created a cohort in workspace "one"
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And an ies account "Bob" is provisioned
    And "Bob" autoenroll to cohort "cohort"
    When "Bob" fetches the learner element ancestry for "COMPONENT_ONE" in deployment "DEPLOYMENT_ONE"
    Then the learner element type is "COMPONENT" and the ancestry has
      | SCREEN_TWO |
      | LINEAR_ONE |
      | UNIT_ONE   |
