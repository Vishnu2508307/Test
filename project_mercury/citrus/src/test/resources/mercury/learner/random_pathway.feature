Feature: Random pathway flow and functionality

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "RANDOM" pathway named "RANDOM_ONE" for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "RANDOM_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "RANDOM_ONE" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "RANDOM_ONE" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "RANDOM_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "SCREEN_TWO" student scope
    And "Alice" has registered "SCREEN_THREE" "INTERACTIVE" element to "SCREEN_THREE" student scope
    And "Alice" has registered "SCREEN_FOUR" "INTERACTIVE" element to "SCREEN_FOUR" student scope
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_TWO" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_TWO           |
      | studentScopeURN   | SCREEN_TWO           |
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_THREE" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_THREE         |
      | studentScopeURN   | SCREEN_THREE         |
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_FOUR" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_FOUR          |
      | studentScopeURN   | SCREEN_FOUR          |
    And "Alice" has set 2 as exit after condition for the "RANDOM_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: It should return a random walkable when starting the pathway
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "RANDOM_WALKABLE_ONE" is supplied between
      | SCREEN_ONE   |
      | SCREEN_TWO   |
      | SCREEN_THREE |
      | SCREEN_FOUR  |

  Scenario: It should return the walkable in progress
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "RANDOM_WALKABLE_ONE" is supplied between
      | SCREEN_ONE   |
      | SCREEN_TWO   |
      | SCREEN_THREE |
      | SCREEN_FOUR  |
    When "Alice" has set "RANDOM_WALKABLE_ONE" studentScope using element "RANDOM_WALKABLE_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":7, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "RANDOM_WALKABLE_ONE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | incorrect           |
      | interactiveName      | RANDOM_WALKABLE_ONE |
      | deploymentName       | DEPLOYMENT_ONE      |
      | interactiveCompleted | false               |
      | fired_scenarios      | 1                   |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "RANDOM_WALKABLE_TWO" is supplied between
      | RANDOM_WALKABLE_ONE |

  Scenario: It should return a random walkable excluding completed walkables and none when pathway completed
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "RANDOM_WALKABLE_ONE" is supplied between
      | SCREEN_ONE   |
      | SCREEN_TWO   |
      | SCREEN_THREE |
      | SCREEN_FOUR  |
    When "Alice" has set "RANDOM_WALKABLE_ONE" studentScope using element "RANDOM_WALKABLE_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "RANDOM_WALKABLE_ONE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct             |
      | interactiveName      | RANDOM_WALKABLE_ONE |
      | deploymentName       | DEPLOYMENT_ONE      |
      | interactiveCompleted | true                |
      | fired_scenarios      | 1                   |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "RANDOM_WALKABLE_TWO" is supplied excluding
      | RANDOM_WALKABLE_ONE |
    When "Alice" has set "RANDOM_WALKABLE_TWO" studentScope using element "RANDOM_WALKABLE_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "RANDOM_WALKABLE_TWO" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct             |
      | interactiveName      | RANDOM_WALKABLE_TWO |
      | deploymentName       | DEPLOYMENT_ONE      |
      | interactiveCompleted | true                |
      | fired_scenarios      | 1                   |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then no walkables are returned