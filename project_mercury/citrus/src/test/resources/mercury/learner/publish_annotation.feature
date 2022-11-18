Feature: Publishing courseware annotations

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has uploaded special "Image" asset once

  Scenario: It should successfully publish the identifying annotation motivation from courseware to learner
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a courseware annotation "ONE" for element "LESSON_ONE" of type "ACTIVITY" with
      | motivation    | identifying       |
      | rootElementId | UNIT_ONE          |
      | target        | {"json":"target"} |
      | body          | {"json":"body"}   |
    When "Alice" has published "UNIT_ONE" activity from a project to "DEPLOYMENT_ONE"
    When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "identifying"
    Then the following deployment annotations are returned
      | ONE |

  Scenario: It should successfully publish the classifying annotation motivation from courseware to learner
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a courseware annotation "ONE" for element "LESSON_TWO" of type "ACTIVITY" with
      | motivation    | classifying       |
      | rootElementId | UNIT_ONE          |
      | target        | {"json":"target"} |
      | body          | {"json":"body"}   |
    When "Alice" has published "UNIT_ONE" activity from a project to "DEPLOYMENT_ONE"
    When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "classifying"
    Then the following deployment annotations are returned
      | ONE |

  Scenario: It should successfully publish the linking annotation motivation from courseware to learner
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a courseware annotation "ONE" for element "SCREEN_ONE" of type "INTERACTIVE" with
      | motivation    | linking           |
      | rootElementId | UNIT_ONE          |
      | target        | {"json":"target"} |
      | body          | {"json":"body"}   |
    When "Alice" has published "UNIT_ONE" activity from a project to "DEPLOYMENT_ONE"
    When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "linking"
    Then the following deployment annotations are returned
      | ONE |

  Scenario: It should successfully publish the tagging annotation motivation from courseware to learner
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a courseware annotation "ONE" for element "COMPONENT_ONE" of type "COMPONENT" with
      | motivation    | tagging           |
      | rootElementId | UNIT_ONE          |
      | target        | {"json":"target"} |
      | body          | {"json":"body"}   |
    And "Alice" has published "UNIT_ONE" activity from a project to "DEPLOYMENT_ONE"
    When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "tagging"
    Then the following deployment annotations are returned
      | ONE |

    Scenario: It should update published annotations
      Given "Alice" has created project "TRO" in workspace "one"
      And "Alice" has created activity "UNIT_ONE" inside project "TRO"
      And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
      And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
      And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
      And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
      And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
      And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
      And "Alice" has created a courseware annotation "ONE" for element "LESSON_ONE" of type "ACTIVITY" with
        | motivation    | linking           |
        | rootElementId | UNIT_ONE          |
        | target        | {"json":"target"} |
        | body          | {"json":"body"}   |
      And "Alice" has created a courseware annotation "TWO" for element "COMPONENT_ONE" of type "COMPONENT" with
        | motivation    | linking           |
        | rootElementId | UNIT_ONE          |
        | target        | {"json":"target"} |
        | body          | {"json":"body"}   |
      And "Alice" has published "UNIT_ONE" activity from a project to "DEPLOYMENT_ONE"
      When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "linking"
      Then the following deployment annotations are returned
        | ONE |
        | TWO |
      Given "Alice" has deleted courseware annotation "ONE"
      And "Alice" has published "UNIT_ONE" activity from a project to "DEPLOYMENT_ONE"
      When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "linking"
      Then the following deployment annotations are returned
        | TWO |

