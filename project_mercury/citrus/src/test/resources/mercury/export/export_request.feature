#Feature: The export request
#
#  Background:
#    Given an account "Alice" is created
#    And "Alice" has created workspace "one"
#    And "Alice" has created and published course plugin
#    And "Alice" has created project "TRO" in workspace "one"
#    And "Alice" has created activity "LESSON_ONE" inside project "TRO"
#    And "Alice" has created a "LINEAR_ONE" pathway for the "LESSON_ONE" activity
#    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
#    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
#    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_ONE" pathway
#    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
#    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "SCREEN_TWO" student scope
#    And "Alice" has registered "SCREEN_THREE" "INTERACTIVE" element to "SCREEN_THREE" student scope
#    And "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with
#      | description     | a correct scenario   |
#      | lifecycle       | INTERACTIVE_EVALUATE |
#      | correctness     | correct              |
#      | expected        | 8                    |
#      | sourceId        | SCREEN_ONE           |
#      | studentScopeURN | SCREEN_ONE           |
#    And "Alice" has created a "TWO_LINEAR_ONE" pathway for the "LESSON_ONE" activity
#    And "Alice" has created a "TWO_SCREEN_ONE" interactive for the "TWO_LINEAR_ONE" pathway
#    And "Alice" has created a "TWO_SCREEN_TWO" interactive for the "TWO_LINEAR_ONE" pathway
#    And "Alice" has created a "TWO_SCREEN_THREE" interactive for the "TWO_LINEAR_ONE" pathway
#    And "Alice" has registered "TWO_SCREEN_ONE" "INTERACTIVE" element to "TWO_SCREEN_ONE" student scope
#    And "Alice" has registered "TWO_SCREEN_TWO" "INTERACTIVE" element to "TWO_SCREEN_TWO" student scope
#    And "Alice" has registered "TWO_SCREEN_THREE" "INTERACTIVE" element to "TWO_SCREEN_THREE" student scope
#    And "Alice" has created a scenario "TWO_CORRECT_SCENARIO_ONE" for the "TWO_SCREEN_ONE" interactive with
#      | description     | a correct scenario   |
#      | lifecycle       | INTERACTIVE_EVALUATE |
#      | correctness     | correct              |
#      | expected        | 8                    |
#      | sourceId        | TWO_SCREEN_ONE       |
#      | studentScopeURN | TWO_SCREEN_ONE       |
#    And "Alice" has created a "THREE_LINEAR_ONE" pathway for the "LESSON_ONE" activity
#    And "Alice" has created a "THREE_SCREEN_ONE" interactive for the "THREE_LINEAR_ONE" pathway
#    And "Alice" has created a "THREE_SCREEN_TWO" interactive for the "THREE_LINEAR_ONE" pathway
#    And "Alice" has created a "THREE_SCREEN_THREE" interactive for the "THREE_LINEAR_ONE" pathway
#    And "Alice" has registered "THREE_SCREEN_ONE" "INTERACTIVE" element to "THREE_SCREEN_ONE" student scope
#    And "Alice" has registered "THREE_SCREEN_TWO" "INTERACTIVE" element to "THREE_SCREEN_TWO" student scope
#    And "Alice" has registered "THREE_SCREEN_THREE" "INTERACTIVE" element to "THREE_SCREEN_THREE" student scope
#    And "Alice" has created a scenario "THREE_CORRECT_SCENARIO_ONE" for the "THREE_SCREEN_ONE" interactive with
#      | description     | a correct scenario   |
#      | lifecycle       | INTERACTIVE_EVALUATE |
#      | correctness     | correct              |
#      | expected        | 8                    |
#      | sourceId        | TWO_SCREEN_ONE       |
#      | studentScopeURN | TWO_SCREEN_ONE       |
#    And "Alice" has created a "FOUR_LINEAR_ONE" pathway for the "LESSON_ONE" activity
#    And "Alice" has created a "FOUR_SCREEN_ONE" interactive for the "FOUR_LINEAR_ONE" pathway
#    And "Alice" has created a "FOUR_SCREEN_TWO" interactive for the "FOUR_LINEAR_ONE" pathway
#    And "Alice" has created a "FOUR_SCREEN_THREE" interactive for the "FOUR_LINEAR_ONE" pathway
#    And "Alice" has registered "FOUR_SCREEN_ONE" "INTERACTIVE" element to "FOUR_SCREEN_ONE" student scope
#    And "Alice" has registered "FOUR_SCREEN_TWO" "INTERACTIVE" element to "FOUR_SCREEN_TWO" student scope
#    And "Alice" has registered "FOUR_SCREEN_THREE" "INTERACTIVE" element to "FOUR_SCREEN_THREE" student scope
#    And "Alice" has created a scenario "FOUR_CORRECT_SCENARIO_ONE" for the "FOUR_SCREEN_ONE" interactive with
#      | description     | a correct scenario   |
#      | lifecycle       | INTERACTIVE_EVALUATE |
#      | correctness     | correct              |
#      | expected        | 8                    |
#      | sourceId        | TWO_SCREEN_ONE       |
#      | studentScopeURN | TWO_SCREEN_ONE       |
#    And "Alice" has created a "FIVE_LINEAR_ONE" pathway for the "LESSON_ONE" activity
#    And "Alice" has created a "FIVE_SCREEN_ONE" interactive for the "FIVE_LINEAR_ONE" pathway
#    And "Alice" has created a "FIVE_SCREEN_TWO" interactive for the "FIVE_LINEAR_ONE" pathway
#    And "Alice" has created a "FIVE_SCREEN_THREE" interactive for the "FIVE_LINEAR_ONE" pathway
#    And "Alice" has registered "FIVE_SCREEN_ONE" "INTERACTIVE" element to "FIVE_SCREEN_ONE" student scope
#    And "Alice" has registered "FIVE_SCREEN_TWO" "INTERACTIVE" element to "FIVE_SCREEN_TWO" student scope
#    And "Alice" has registered "FIVE_SCREEN_THREE" "INTERACTIVE" element to "FIVE_SCREEN_THREE" student scope
#    And "Alice" has created a scenario "FIVE_CORRECT_SCENARIO_ONE" for the "FIVE_SCREEN_ONE" interactive with
#      | description     | a correct scenario   |
#      | lifecycle       | INTERACTIVE_EVALUATE |
#      | correctness     | correct              |
#      | expected        | 8                    |
#      | sourceId        | TWO_SCREEN_ONE       |
#      | studentScopeURN | TWO_SCREEN_ONE       |
#
#
  # ignore this test so it doesn't get executed in the pipeline for now
#  @ignore
#  Scenario: The owner of an courseware should be able to successfully export a course
#    Given "Alice" has requested a "LESSON_TWO" "ACTIVITY" export as "EXPORT_ONE"
#    And "Alice" has subscribed to export "EXPORT_ONE"
#    When "Alice" gets an export broadcast for "EXPORT_ONE"
#    Then "Alice" unsubscribe to export "EXPORT_ONE"
#    And "Alice" verifies the export status for "EXPORT_ONE" in project "TRO" is "COMPLETED"

#  Scenario: The owner of an courseware should be able to successfully export a course
#    Given "Alice" has requested a "LESSON_ONE" "ACTIVITY" export "5" times
#    Then the "1" export ids are printed "TRO"
