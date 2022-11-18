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
    And "Alice" has published "Course" activity to "CourseDeployment"

  Scenario: User should be able to fetch learn activity through RTM
    When "Alice" sends a GraphQL message
     """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              id
              config
              plugin(view: "LEARNER") {
                entryPoints {
                  entryPointData
                  entryPointPath
                  contentType
                  context
                }
                manifest {
                  configurationSchema
                }
              }
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
              "activity": {
                "id": "${Course_id}",
                "config": "@notEmpty()@",
                "plugin": {
                  "entryPoints": [
                    {
                      "entryPointData": "some random content",
                      "entryPointPath": "@notEmpty()@",
                      "contentType": "javascript",
                      "context": "LEARNER"
                    }
                  ],
                  "manifest": {
                    "configurationSchema": "@notEmpty()@"
                  }
                }
              }
            }
          ]
        }
      }
    }
    """

  Scenario: User should be able to fetch learn activity by its id through RTM
    When "Alice" sends a GraphQL message
     """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity(activityId: "${Course_id}") {
              id
              config
              plugin(view: "LEARNER") {
                entryPoints {
                  entryPointData
                  entryPointPath
                  contentType
                  context
                }
                manifest {
                  configurationSchema
                }
              }
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
              "activity": {
                "id": "${Course_id}",
                "config": "@notEmpty()@",
                "plugin": {
                  "entryPoints": [
                    {
                      "entryPointData": "some random content",
                      "entryPointPath": "@notEmpty()@",
                      "contentType": "javascript",
                      "context": "LEARNER"
                    }
                  ],
                  "manifest": {
                    "configurationSchema": "@notEmpty()@"
                  }
                }
              }
            }
          ]
        }
      }
    }
    """
