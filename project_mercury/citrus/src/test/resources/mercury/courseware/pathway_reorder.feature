Feature: Reordering of walkables in a pathway

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT" inside project "TRO"

  Scenario: User should be able to reorder pathways and event is broadcasted
    Given "Alice" has created a "LINEAR_1" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_1" pathway
    And "Alice" has created a "UNIT_ONE" activity for the "LINEAR_1" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_1" pathway
    And "Bob" subscribes to "activity" events for "UNIT" via a "Bob" client
    When "Alice" has reordered walkables for the "LINEAR_1" pathway
      | UNIT_ONE   |
      | SCREEN_TWO |
      | SCREEN_ONE |
    Then the "LINEAR_1" pathway payload contains reordered walkables
      | UNIT_ONE   |
      | SCREEN_TWO |
      | SCREEN_ONE |
    And "Bob" should receive an action "PATHWAY_REORDERED" message for the "LINEAR_1" pathway with walkables
      | UNIT_ONE   | ACTIVITY    |
      | SCREEN_TWO | INTERACTIVE |
      | SCREEN_ONE | INTERACTIVE |
