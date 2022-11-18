Feature: Interactive deletion

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: User should be able to delete an interactive with a parent pathway
    Given "Alice" has created activity "LESSON" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "LESSON" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR" pathway
    And "Alice" has created a "UNIT" activity for the "LINEAR" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a courseware "identifying" annotation "ONE" for element "SCREEN_ONE" and rootElement "LESSON" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "TWO" for element "COMPONENT_ONE" and rootElement "LESSON" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Alice" deletes the "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    Then the "SCREEN_ONE" interactive is deleted
    # Deleting the annotations happens after the interactive is deleted
    # give it a second to be sure the deletion completed
    And "Alice" wait 1 seconds
    Then "Alice" can not list annotation with motivation "identifying" for element "SCREEN_ONE" and rootElement "LESSON"
    And "LINEAR" pathway does not include interactive "SCREEN_ONE" in its children anymore
    And "Alice" can not fetch "SCREEN_ONE" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"

  Scenario: User should not be able to delete an interactive with wrong pathway
    Given "Alice" has created activity "LESSON" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "LESSON" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON" activity
    When "Alice" deletes the "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    Then the "SCREEN_ONE" delete fails due to code 400 and message "supplied parentPathwayId does not match the interactive parent"
