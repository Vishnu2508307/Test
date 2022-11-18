Feature: It should evaluate all the scenarios for an INTERACTIVE_EVALUATE lifecycle

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
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
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
      | description     | a correct scenario   |
      | lifecycle       | INTERACTIVE_EVALUATE |
      | correctness     | incorrect            |
      | expected        | 9                    |
      | sourceId        | SCREEN_ONE           |
      | studentScopeURN | SCREEN_ONE           |
    And "Alice" has created a scenario "CORRECT_SCENARIO_THREE" for the "SCREEN_TWO" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
      | awardDocumentItem | ITEM_ONE             |
      | awardFromDocument | SKILLS               |
    And "Alice" has linked to "INTERACTIVE" "SCREEN_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: It should correctly evaluate an interactive
    Given "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    And "Alice" should have been awarded the following competency items from "SKILLS" document
      | ITEM_ONE    | 1.0 |
      | PARENT_ITEM | 0.5 |
    When "Alice" fetches the completed walkable history for "LINEAR_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | SCREEN_ONE |
    When "Alice" fetches the evaluation "SCREEN_ONE_EVALUATION" on deployment "DEPLOYMENT_ONE"
    Then evaluation "SCREENT_ONE_EVALUATION" is returned correctly
    When "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_TWO     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    When "Alice" fetches the completed walkable history for "LINEAR_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | SCREEN_TWO |
      | SCREEN_ONE |

  Scenario: It should update the score when evaluating an interactive
    Given instructor "Alice" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 0             |
      | reason | NOT_ATTEMPTED |
    When "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":9, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | incorrect      |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 2              |
    When instructor "Alice" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 0      |
      | reason | SCORED |
    When "Alice" fetches "LINEAR_ONE" pathway walkable score for deployment "DEPLOYMENT_ONE", the scores is
      | value  | 0      |
      | reason | SCORED |
    When "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    When instructor "Alice" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 10     |
      | reason | SCORED |
    When "Alice" has set "SCREEN_TWO" studentScope using element "SCREEN_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":9, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_TWO     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    When instructor "Alice" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 20     |
      | reason | SCORED |
    When "Alice" fetches the score for "LINEAR_ONE" pathway for deployment "DEPLOYMENT_ONE", the score is
      | value  | 20     |
      | reason | SCORED |

  Scenario: It should evaluate an interactive and return incorrect when the scope is unset
    Given "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | incorrect                    |
      | interactiveName      | SCREEN_ONE                   |
      | deploymentName       | DEPLOYMENT_ONE               |
      | interactiveCompleted | false                        |
      | fired_scenarios      | 2                            |
      | errorMessage         | scope id not found for Alice |

  Scenario: It should evaluate an interactive and return incorrect when the scope is empty
    Given "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":[], "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | incorrect      |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 2              |

  Scenario: It should evaluate an interactive and return null correctness when they all evaluate to false
    Given "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":4, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | null           |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 2              |

  Scenario: It should evaluate an interactive and return interactiveComplete true when there are no scenarios
    Given "Alice" has set "SCREEN_THREE" studentScope using element "SCREEN_THREE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":5, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "SCREEN_THREE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | null                 |
      | interactiveName      | SCREEN_THREE         |
      | deploymentName       | DEPLOYMENT_ONE       |
      | interactiveCompleted | true                 |
      | fired_scenarios      | 0                    |
      | triggeredActionsSize | 1                    |
      | defaultProgression   | INTERACTIVE_COMPLETE |

  Scenario: It should allow the learner to progress to the next screen once a screen is completed
    When "Alice" gets the next walkable from
      | deploymentName | DEPLOYMENT_ONE |
      | pathwayName    | LINEAR_ONE     |
      | cohortName     | cohort         |
    Then "SCREEN_ONE" interactive is returned
    Given "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" has successfully evaluated interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    When "Alice" gets the next walkable from
      | deploymentName | DEPLOYMENT_ONE |
      | pathwayName    | LINEAR_ONE     |
      | cohortName     | cohort         |
    Then "SCREEN_TWO" interactive is returned
