Feature: Activity deletion

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: User should be able to delete an activity with a parent pathway
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    #for LESSON activity create PATHWAY and for that pathway create interactive and create annotations and assert bt calling annotations.list with LESSON activity
    And "Alice" has created a "LINEAR_ONE" pathway for the "LESSON" activity
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_TWO" activity
    And "Alice" has created a "LESSON_THREE" activity for the "LINEAR_TWO" pathway
    And "Alice" has created a "LINEAR_THREE" pathway for the "LESSON_THREE" activity
    And "Alice" has created a "LINEAR_ONE_SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_ONE_SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO_SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "LINEAR_TWO_SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "LINEAR_THREE_SCREEN_ONE" interactive for the "LINEAR_THREE" pathway
    And "Alice" has created a "LINEAR_THREE_SCREEN_TWO" interactive for the "LINEAR_THREE" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "LINEAR_THREE_SCREEN_TWO" interactive
    And "Alice" has created a courseware "identifying" annotation "ONE" for element "LINEAR_ONE_SCREEN_ONE" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "TWO" for element "LINEAR_ONE_SCREEN_TWO" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "THREE" for element "LINEAR_TWO_SCREEN_ONE" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "FOUR" for element "LINEAR_TWO_SCREEN_TWO" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "FIVE" for element "LESSON" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "SIX" for element "LESSON_TWO" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "SEVEN" for element "LINEAR_THREE_SCREEN_ONE" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "EIGHT" for element "LINEAR_THREE_SCREEN_TWO" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "NINE" for element "COMPONENT_ONE" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Alice" deletes the "LESSON" activity for the "LINEAR" pathway
    Then the "LESSON" activity is deleted
    And the "LESSON" activity does not have a parent pathway
    And the "LINEAR" pathway does not have the "LESSON" activity as children
    And "Alice" wait 1 seconds
    Then "Alice" can not list "identifying" annotation for rootElement "UNIT"

  Scenario: User should not be able to delete an activity with no parent pathway - a different message should be used
    Given "Alice" has created activity "UNIT" inside project "TRO"
    When "Alice" deletes the "UNIT" activity
    Then the activity deleting fails with message "parentPathwayId not found for activity ${UNIT_id}" and code 400

  Scenario: User should be able to delete annotations by rootElementId in project activity
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    And "Alice" has created a courseware "classifying" annotation "LESSON_ONE" for element "LESSON" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a "LINEAR_ONE" pathway for the "LESSON" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a courseware "classifying" annotation "SCREEN_ONE_ONE" for element "SCREEN_ONE" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_TWO" activity
    And "Alice" has created a "SCREEN_TWO" activity for the "LINEAR_TWO" pathway
    And "Alice" has created a courseware "classifying" annotation "SCREEN_TWO_ONE" for element "SCREEN_TWO" and rootElement "UNIT" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then "Alice" can list following fields from courseware annotation "LESSON_ONE" with motivation "classifying" and rootElement "UNIT"
      | motivation | classifying|
      | target     | target   |
      | body       | body     |
    When "Alice" deletes activity "UNIT" from project "TRO"
    Then activity "UNIT" is successfully deleted from project "TRO"

  Scenario: User should be able to delete an activity in a project
    Given "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    When "Alice" deletes activity "ACTIVITY_ONE" from project "TRO"
    Then activity "ACTIVITY_ONE" is successfully deleted from project "TRO"
    When she fetches the "ACTIVITY_ONE" activity
    Then "Alice" can not fetch deleted "ACTIVITY_ONE" activity

  Scenario: User should not be able to delete an activity in a project more than once
    Given "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    When "Alice" deletes activity "ACTIVITY_ONE" from project "TRO"
    Then activity "ACTIVITY_ONE" is successfully deleted from project "TRO"
    When "Alice" deletes activity "ACTIVITY_ONE" from project "TRO"
    Then activity "ACTIVITY_ONE" is not deleted from project "TRO"
    When she fetches the "ACTIVITY_ONE" activity
    Then "Alice" can not fetch deleted "ACTIVITY_ONE" activity
