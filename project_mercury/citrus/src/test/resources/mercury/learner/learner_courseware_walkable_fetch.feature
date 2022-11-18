Feature: Fetch the learner courseware walkable structure for an element

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And a workspace account "Bob" is created
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has uploaded special "Image" asset once
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"


  Scenario: It should allow a user to fetch learner courseware structure for LINEAR pathway
    When "Alice" replace the activity "UNIT_ONE" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    When "Alice" created a "LINEAR" pathway for the activity "UNIT_ONE" with preload pathway "ALL"
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    When "Alice" created a "LINEAR_ONE" pathway for the activity "LESSON" with preload pathway "ALL"
    When "Alice" created a "LINEAR_TWO" pathway for the activity "LESSON" with preload pathway "NONE"
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "WIDGET_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "WIDGET_TWO" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "INPUT_THREE" component for the "SCREEN_THREE" interactive
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE" "UNIT_ONE"

  Scenario: It should allow a user to fetch learner courseware structure for LINEAR pathway from interactive
    When "Alice" created a "LINEAR" pathway for the activity "UNIT_ONE" with preload pathway "ALL"
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    When "Alice" created a "LINEAR_ONE" pathway for the activity "LESSON" with preload pathway "ALL"
    When "Alice" created a "LINEAR_TWO" pathway for the activity "LESSON" with preload pathway "NONE"
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "WIDGET_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "WIDGET_TWO" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "INPUT_THREE" component for the "SCREEN_THREE" interactive
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE" and interactive "SCREEN_ONE"

  Scenario: It should allow a user to fetch learner courseware structure for LINEAR pathway with missing element
    When "Alice" replace the activity "UNIT_ONE" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    When "Alice" created a "LINEAR" pathway for the activity "UNIT_ONE" with preload pathway "ALL"
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    When "Alice" created a "LINEAR_ONE" pathway for the activity "LESSON" with preload pathway "ALL"
    When "Alice" created a "LINEAR_TWO" pathway for the activity "LESSON" with preload pathway "NONE"
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "WIDGET_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "WIDGET_TWO" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "INPUT_THREE" component for the "SCREEN_THREE" interactive
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE"

  Scenario: It should allow a user to fetch learner courseware structure for LINEAR pathway with non root element
    And "Alice" has created a "FREE" pathway named "LINEAR" for the "UNIT_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    And "Alice" has created a "FREE" pathway named "LINEAR_ONE" for the "LESSON" activity with preload pathway "ALL"
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    When "Alice" replace the activity "LESSON_ONE" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    And "Alice" has created a "FREE" pathway named "LINEAR_TWO" for the "LESSON_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "WIDGET_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "WIDGET_TWO" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "WIDGET_THREE" component for the "SCREEN_TWO" interactive
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE" "LESSON_ONE"

  Scenario: It should allow a user to fetch learner courseware structure for FREE pathway
    When "Alice" replace the activity "UNIT_ONE" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    And "Alice" has created a "FREE" pathway named "UNIT_PATHWAY" for the "UNIT_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "LESSON_ONE" activity for the "UNIT_PATHWAY" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "UNIT_PATHWAY" pathway
    And "Alice" has created a "FREE" pathway named "LESSON_PATHWAY" for the "LESSON_ONE" activity with preload pathway "FIRST"
    And "Alice" has created a "SCREEN_ONE" interactive for the "LESSON_PATHWAY" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LESSON_PATHWAY" pathway
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE" "UNIT_ONE"

  Scenario: It should allow a user to fetch learner courseware structure for FREE pathway with non root element
    And "Alice" has created a "FREE" pathway named "UNIT_PATHWAY" for the "UNIT_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "LESSON_ONE" activity for the "UNIT_PATHWAY" pathway
    When "Alice" replace the activity "LESSON_ONE" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    And "Alice" has created a "LESSON_TWO" activity for the "UNIT_PATHWAY" pathway
    And "Alice" has created a "FREE" pathway named "LESSON_PATHWAY" for the "LESSON_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "SCREEN_ONE" interactive for the "LESSON_PATHWAY" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LESSON_PATHWAY" pathway
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE" "LESSON_ONE"

  Scenario: It should allow a user to fetch learner courseware structure for GRAPH pathway
    When "Alice" replace the activity "UNIT_ONE" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    And "Alice" has created a "GRAPH" pathway named "PATHWAY_ONE" for the "UNIT_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "INTERACTIVE_ONE" interactive for the "PATHWAY_ONE" pathway
    And "Alice" has created a "ACTIVITY_TWO" activity for the "PATHWAY_ONE" pathway
    And "Alice" has created a "INTERACTIVE_FOUR" interactive for the "PATHWAY_ONE" pathway
    And "Alice" has created a "GRAPH" pathway named "PATHWAY_TWO" for the "ACTIVITY_TWO" activity with preload pathway "FIRST"
    And "Alice" has created a "INTERACTIVE_TWO" interactive for the "PATHWAY_TWO" pathway
    And "Alice" has created a "INTERACTIVE_THREE" interactive for the "PATHWAY_TWO" pathway
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE"

  Scenario: It should allow a user to fetch learner courseware structure for GRAPH pathway with ALL preload and non root element
    And "Alice" has created a "GRAPH" pathway named "PATHWAY_ONE" for the "UNIT_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "INTERACTIVE_ONE" interactive for the "PATHWAY_ONE" pathway
    And "Alice" has created a "ACTIVITY_TWO" activity for the "PATHWAY_ONE" pathway
    When "Alice" replace the activity "ACTIVITY_TWO" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    And "Alice" has created a "INTERACTIVE_FOUR" interactive for the "PATHWAY_ONE" pathway
    And "Alice" has created a "GRAPH" pathway named "PATHWAY_TWO" for the "ACTIVITY_TWO" activity with preload pathway "ALL"
    And "Alice" has created a "INTERACTIVE_TWO" interactive for the "PATHWAY_TWO" pathway
    And "Alice" has created a "INTERACTIVE_THREE" interactive for the "PATHWAY_TWO" pathway
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE" "ACTIVITY_TWO"

  Scenario: It should allow a user to fetch learner courseware structure for RANDOM pathway with ALL preload
    When "Alice" replace the activity "UNIT_ONE" theme with
      | color | blue |
    Then the activity theme is replaced successfully
    And "Alice" has created a "RANDOM" pathway named "RANDOM_ONE" for the "UNIT_ONE" activity with preload pathway "ALL"
    And "Alice" has created a "SCREEN_ONE" interactive for the "RANDOM_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "RANDOM_ONE" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "RANDOM_ONE" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "RANDOM_ONE" pathway
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetches learner element structure for "DEPLOYMENT_ONE" "UNIT_ONE"
