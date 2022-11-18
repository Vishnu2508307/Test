@Learn
Feature: Fetch learn activity in Learn context for math assets

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "Course" inside project "TRO"

    And "Alice" creates a math asset for the "Course" "activity" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    And the math asset "MATH_ASSET_ONE" is successfully created

    And "Alice" has created a "Pathway" pathway for the "Course" activity
    And "Alice" has created a "Walkable1" activity for the "Pathway" pathway
    And "Alice" has created a "Walkable2" interactive for the "Pathway" pathway
    And "Alice" has created a "Field" component for the "Walkable2" interactive

    And "Alice" creates a math asset for the "Walkable1" "activity" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    And the math asset "MATH_ASSET_ONE" is successfully created

    And "Alice" creates a math asset for the "Walkable2" "interactive" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    And the math asset "MATH_ASSET_ONE" is successfully created

    And "Alice" creates a math asset for the "Field" "component" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    And the math asset "MATH_ASSET_ONE" is successfully created

    And "Alice" has published "Course" activity to "CourseDeployment"
    And a support role account "Charlie" exists
    And a mycloud account "Charlie" is provisioned

  Scenario: User should fetch learn activity along with first level math assets through RTM
    When "Charlie" sends a GraphQL message and authenticate via my cloud
    """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              id
              mathAssets {
                edges {
                  node {
                    urn
                  }
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
                "mathAssets": {
                  "edges": [
                  {
                    "node": {
                      "urn": "@notEmpty()@"
                    }
                  }
                  ]
                }
              }
            }
          ]
        }
      }
    }
    """

  Scenario: User should fetch learn activity along with second level math assets through RTM
    When "Charlie" sends a GraphQL message and authenticate via my cloud
    """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              id
              mathAssets {
                edges {
                  node {
                    urn
                  }
                }
              }
              pathways {
                walkables {
                  edges {
                    node {
                      id
                      mathAssets {
                        edges {
                          node {
                            urn
                          }
                        }
                      }
                    }
                  }
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
                "mathAssets": {
                  "edges": [
                  {
                    "node": {
                      "urn": "@notEmpty()@"
                    }
                  }
                  ]
                },
                "pathways": [
                  {
                    "walkables": {
                      "edges": [
                        {
                          "node": {
                            "id": "${Walkable1_id}",
                            "mathAssets": {
                              "edges": [
                                {
                                  "node": {
                                    "urn": "@notEmpty()@"
                                  }
                                }
                              ]
                            }
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
          ]
        }
      }
    }
    """

  Scenario: User should fetch learn activity along with component level math assets through RTM
    When "Charlie" sends a GraphQL message and authenticate via my cloud
    """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              id
              mathAssets {
                edges {
                  node {
                    urn
                  }
                }
              }
              pathways {
                walkables {
                  edges {
                    node {
                      components {
                        mathAssets {
                          edges {
                            node {
                              urn
                            }
                          }
                        }
                      }
                      id
                      mathAssets {
                        edges {
                          node {
                            urn
                          }
                        }
                      }
                    }
                  }
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
                "mathAssets": {
                  "edges": [
                  {
                    "node": {
                      "urn": "@notEmpty()@"
                    }
                  }
                  ]
                },
                "pathways": [
                  {
                    "walkables": {
                      "edges": [
                        {
                          "node": {
                            "id": "${Walkable1_id}",
                            "mathAssets": {
                              "edges": [
                                {
                                  "node": {
                                    "urn": "@notEmpty()@"
                                  }
                                }
                              ]
                            },
                            "components": []
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
          ]
        }
      }
    }
    """
