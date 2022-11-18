Feature: It should evaluate all the scenarios for an INTERACTIVE_EVALUATE lifecycle in COMBINED mode

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created a document item "PARENT_ITEM" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created a document item "BROTHER_ITEM" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE    |
      | destinationItemId | PARENT_ITEM |
      | associationType   | isChildOf   |
    And "Alice" has created an association "ASSOCIATION_TWO" for "SKILLS" document with
      | originItemId      | BROTHER_ITEM |
      | destinationItemId | PARENT_ITEM  |
      | associationType   | isChildOf    |
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created evaluable for "SCREEN_ONE" "INTERACTIVE" with evaluation mode "COMBINED"
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created evaluable for "SCREEN_TWO" "INTERACTIVE" with evaluation mode "COMBINED"
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "SCREEN_TWO" student scope
    And "Alice" has registered "SCREEN_THREE" "INTERACTIVE" element to "SCREEN_THREE" student scope
    And "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
      | awardDocumentItem | ITEM_ONE             |
      | awardFromDocument | SKILLS               |
    And "Alice" has created a scenario "INCORRECT_SCENARIO_TWO" for the "SCREEN_ONE" interactive with
      | description     | an incorrect scenario   |
      | lifecycle       | INTERACTIVE_EVALUATE    |
      | correctness     | incorrect               |
      | expected        | 9                       |
      | sourceId        | SCREEN_ONE              |
      | studentScopeURN | SCREEN_ONE              |
    And "Alice" has created a scenario "CORRECT_SCENARIO_THREE" for the "SCREEN_TWO" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
      | awardDocumentItem | ITEM_ONE             |
      | awardFromDocument | SKILLS               |
    And "Alice" has created a scenario "NONE_SCENARIO_FOUR" for the "SCREEN_TWO" interactive with
      | description     | a none scenario         |
      | lifecycle       | INTERACTIVE_EVALUATE    |
      | correctness     | none                    |
      | expected        | 10                      |
      | sourceId        | SCREEN_ONE              |
      | studentScopeURN | SCREEN_ONE              |
    And "Alice" has linked to "INTERACTIVE" "SCREEN_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: incorrect scenario should take precedence over correct
    Given "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":9, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | incorrect      |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 2              |
      | evaluation_mode      | COMBINED       |
    When "Alice" fetches the completed walkable history for "LINEAR_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | SCREEN_ONE |
    When "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | null           |
      | interactiveName      | SCREEN_TWO     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 2              |
      | evaluation_mode      | COMBINED       |
    When "Alice" fetches the completed walkable history for "LINEAR_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | SCREEN_TWO |
      | SCREEN_ONE |

  Scenario: correct scenario should take precedence over none
    Given "Alice" has set "SCREEN_TWO" studentScope using element "SCREEN_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" has set "SCREEN_TWO" studentScope using element "SCREEN_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":10, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_TWO     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 2              |
      | evaluation_mode      | COMBINED       |
    When "Alice" fetches the completed walkable history for "LINEAR_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | SCREEN_TWO |
