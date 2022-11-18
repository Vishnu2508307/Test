Feature: Testing the passport integration

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"

  Scenario: It should create a redirect when the activity is published and the cohort settings have productId
    And "Alice" has created a cohort "PASSPORT_TEST" with values
      | name             | Learner redirect test             |
      | enrollmentType   | PASSPORT                          |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | workspaceId      | ${one_workspace_id}               |
      | productId        | mercury:randomTimeUUID()          |
    And "Alice" has created project "APOLLO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "APOLLO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to cohort "PASSPORT_TEST" from a project with name "DEPLOYMENT_ONE"
    # the time for the event publisher to create the redirect which is an async op
    Given "Alice" wait 1 seconds
    And an ies account "Bob" is provisioned
    When "Bob" sends a GraphQL message
     """
    {
      learn {
        cohort(cohortId: "${PASSPORT_TEST_id}") {
          id
          name
          deployment(deploymentId: "${DEPLOYMENT_ONE_id}") {
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
          "id": "${PASSPORT_TEST_id}",
          "name": "Learner redirect test",
          "deployment": [
            {
              "activity": {
                "id": "${UNIT_ONE_id}",
                "config": null,
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
