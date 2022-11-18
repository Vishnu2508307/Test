@Learn
Feature: Fetch learn activity in Learn context

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "Course" inside project "TRO"
    And "Alice" has saved configuration for "Course" activity
    And "Alice" has created a "LINEAR_ONE" pathway for the "Course" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "Course" activity to "CourseDeployment"

  Scenario: User should be able to fetch learn interactive by its id through RTM
    When "Alice" sends a GraphQL message
     """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            interactive(interactiveId: "${SCREEN_ONE_id}") {
              id
              elementType
            }
          }
        }
      }
    }
    """
    Then mercury should respond with a GraphQL response
    """
    {
      "learn": {
        "cohort": {
          "id": "${cohort_id}",
          "name": "Citrus cohort",
          "deployment": [
            {
              "interactive": {
                "id": "${SCREEN_ONE_id}",
                "elementType": "INTERACTIVE"
              }
            }
          ]
        }
      }
    }
    """
