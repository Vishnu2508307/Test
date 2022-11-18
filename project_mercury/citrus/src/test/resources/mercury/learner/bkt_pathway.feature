Feature: BKT pathway flow and functionality

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created a document item "PARENT_ITEM" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE    |
      | destinationItemId | PARENT_ITEM |
      | associationType   | isChildOf   |
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "ALGO_BKT" pathway named "BKT_ONE" for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "BKT_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "BKT_ONE" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "BKT_ONE" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "BKT_ONE" pathway
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
    And "Alice" has set config for "BKT_ONE" pathway with
      """
      {"exitAfter": 2, "P_G": 0.2, "P_S": 0.2, "P_T": 0.3, "P_L0": 0.4, "P_LN": 0.85, "maintainFor": 2, "competency": [
        {"documentId": "${SKILLS_id}", "documentItemId": "${ITEM_ONE_id}"}
      ]}
      """
    # this step is so that the whole document is published. Currently a document is published only when a link
    # exists between one of the CE being published and a document item. In this case the document item is referenced
    # inside the BKT pathway config and it should probably be published (need to double check business logic for this)
    And "Alice" has linked to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: It should return a random walkable when starting the pathway
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "BKT_WALKABLE_ONE" is supplied between
      | SCREEN_ONE   |
      | SCREEN_TWO   |
      | SCREEN_THREE |
      | SCREEN_FOUR  |

  Scenario: It should return the walkable in progress
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "BKT_WALKABLE_ONE" is supplied between
      | SCREEN_ONE   |
      | SCREEN_TWO   |
      | SCREEN_THREE |
      | SCREEN_FOUR  |
    When "Alice" has set "BKT_WALKABLE_ONE" studentScope using element "BKT_WALKABLE_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":7, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "BKT_WALKABLE_ONE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | incorrect           |
      | interactiveName      | BKT_WALKABLE_ONE    |
      | deploymentName       | DEPLOYMENT_ONE      |
      | interactiveCompleted | false               |
      | fired_scenarios      | 1                   |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "BKT_WALKABLE_TWO" is supplied between
      | BKT_WALKABLE_ONE |
    And "Alice" should have been awarded the following competency items from "SKILLS" document
      | ITEM_ONE    | 0.4 |
      | PARENT_ITEM | 0.4 |

  Scenario: It should return a random walkable excluding completed walkables and none when pathway completed
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "BKT_WALKABLE_ONE" is supplied between
      | SCREEN_ONE   |
      | SCREEN_TWO   |
      | SCREEN_THREE |
      | SCREEN_FOUR  |
    When "Alice" has set "BKT_WALKABLE_ONE" studentScope using element "BKT_WALKABLE_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "BKT_WALKABLE_ONE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct             |
      | interactiveName      | BKT_WALKABLE_ONE    |
      | deploymentName       | DEPLOYMENT_ONE      |
      | interactiveCompleted | true                |
      | fired_scenarios      | 1                   |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then a "BKT_WALKABLE_TWO" is supplied excluding
      | BKT_WALKABLE_ONE |
    When "Alice" has set "BKT_WALKABLE_TWO" studentScope using element "BKT_WALKABLE_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "BKT_WALKABLE_TWO" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct             |
      | interactiveName      | BKT_WALKABLE_TWO    |
      | deploymentName       | DEPLOYMENT_ONE      |
      | interactiveCompleted | true                |
      | fired_scenarios      | 1                   |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then no walkables are returned
    And "Alice" should have been awarded the following competency items from "SKILLS" document
      | ITEM_ONE    | 0.96100795 |
      | PARENT_ITEM | 0.96100795 |