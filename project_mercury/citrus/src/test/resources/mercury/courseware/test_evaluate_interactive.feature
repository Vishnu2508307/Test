Feature: It should test evaluate all the scenarios for an INTERACTIVE_EVALUATE lifecycle given test scope data

  Scenario: It should test evaluate scenarios on an interactive
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created activity "LESSON_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
    And "Alice" has created a scenario "INCORRECT_SCENARIO_TWO" for the "SCREEN_ONE" interactive with
      | description     | a correct scenario   |
      | lifecycle       | INTERACTIVE_EVALUATE |
      | correctness     | incorrect            |
      | expected        | 9                    |
      | sourceId        | SCREEN_ONE           |
      | studentScopeURN | SCREEN_ONE           |
    When "Alice" test evaluate scenarios for interactive "SCREEN_ONE" with data
        """
        {"${SCREEN_ONE_id}":{"selection":8, "options":{"foo":"bar"}}}
        """
    Then the interactive test evaluation result has
      | is_true         | CORRECT_SCENARIO_ONE   |
      | is_false        | INCORRECT_SCENARIO_TWO |
      | fired_scenarios | 2                      |