Feature: Publish an activity to a learner environment

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has uploaded special "Image" asset once

  Scenario: It should not allow a reviewer to publish
    Given a workspace account "Bob" is created
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" publishes "UNIT_ONE" activity
    Then the activity is not published due to "UNAUTHORIZED" error

  Scenario: It should not allow to publish a non top level activity
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    When "Alice" publishes "LESSON_ONE" activity
    Then the activity is not published due to "BAD REQUEST" error

  Scenario: It should successfully publish an activity
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_FOUR" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_FOUR" for activity "LESSON_TWO"
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: It should not update a deployment when there are no changes detected
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_TWO" for activity "LESSON_TWO"
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" publishes "UNIT_ONE" activity to update "DEPLOYMENT_ONE"
    Then the activity is not published due to "BAD REQUEST" error

  Scenario: It should successfully publish and update an existing deployment
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_TWO" for activity "LESSON_TWO"
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Alice" has created scenario "SCENARIO_THREE" for activity "LESSON_ONE"
    When "Alice" publishes "UNIT_ONE" activity to update "DEPLOYMENT_ONE"
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_TWO"
    And "DEPLOYMENT_TWO" and "DEPLOYMENT_ONE" have different
      | change_id |
    And "DEPLOYMENT_TWO" and "DEPLOYMENT_ONE" have same
      | id          |
      | activity_id |

  Scenario: A user with no permission over the cohort is not allowed to publish an activity
    Given a workspace account "Bob" is created
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created a cohort "class" for workspace "one"
    When "Alice" publishes "UNIT_ONE" activity to cohort "class"
    Then the activity is not published due to "UNAUTHORIZED" error

  Scenario: competency document should be published for each linked document item when publishing a courseware
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill 1 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has created a document item "ITEM_TWO" for "SKILLS" document with
      | fullStatement        | knowledge skill 2 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has created a document item "ITEM_THREE" for "SKILLS" document with
      | fullStatement        | knowledge skill 2 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has created an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    And "Alice" has linked to "ACTIVITY" "LESSON_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    And document item "ITEM_ONE" is published for deployment "DEPLOYMENT_ONE"
    And published document "SKILLS" should contain the following document items
      | ITEM_ONE   |
      | ITEM_TWO   |
      | ITEM_THREE |
    And published document "SKILLS" should contain the following associations
      | ITEM_ONE | ASSOCIATION_ONE |
      | ITEM_TWO | ASSOCIATION_ONE |

    #project activity publish

  Scenario: It should not allow a reviewer to publish an activity inside project
    Given a workspace account "Bob" is created
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" publishes "UNIT_ONE" activity from a project
    Then the activity is not published from a project due to "UNAUTHORIZED" error

  Scenario: It should not allow to publish a non top level activity inside project
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    When "Alice" publishes "LESSON_ONE" activity from a project
    Then the activity is not published from a project due to "BAD REQUEST" error

  Scenario: It should successfully publish an activity inside project
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has updated the config for activity "UNIT_ONE" with
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_FOUR" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_FOUR" for activity "LESSON_TWO"
    When "Alice" publishes "UNIT_ONE" activity from a project
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: It should not update a deployment when there are no changes detected
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_TWO" for activity "LESSON_TWO"
    And "Alice" has published "UNIT_ONE" activity from a project to "DEPLOYMENT_ONE"
    When "Alice" publishes "UNIT_ONE" activity from a project to update "DEPLOYMENT_ONE"
    Then the activity is not published from a project due to "BAD REQUEST" error

  Scenario: It should successfully publish and update an existing deployment inside project
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_TWO" for activity "LESSON_TWO"
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Alice" has created scenario "SCENARIO_THREE" for activity "LESSON_ONE"
    When "Alice" publishes "UNIT_ONE" activity from a project to update "DEPLOYMENT_ONE"
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_TWO"
    And "DEPLOYMENT_TWO" and "DEPLOYMENT_ONE" have different
      | change_id |
    And "DEPLOYMENT_TWO" and "DEPLOYMENT_ONE" have same
      | id          |
      | activity_id |

  Scenario: A user with no permission over the cohort is not allowed to publish an activity inside project
    Given a workspace account "Bob" is created
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Bob" has created a cohort "class" for workspace "one"
    When "Alice" publishes "UNIT_ONE" activity from a project to cohort "class"
    Then the activity is not published from a project due to "UNAUTHORIZED" error

  Scenario: competency document should be published for each linked document item when publishing a courseware inside project
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill 1 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has created a document item "ITEM_TWO" for "SKILLS" document with
      | fullStatement        | knowledge skill 2 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has created a document item "ITEM_THREE" for "SKILLS" document with
      | fullStatement        | knowledge skill 2 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has created an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    And "Alice" has linked to "ACTIVITY" "LESSON_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    When "Alice" publishes "UNIT_ONE" activity from a project
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    And document item "ITEM_ONE" is published for deployment "DEPLOYMENT_ONE"
    And published document "SKILLS" should contain the following document items
      | ITEM_ONE   |
      | ITEM_TWO   |
      | ITEM_THREE |
    And published document "SKILLS" should contain the following associations
      | ITEM_ONE | ASSOCIATION_ONE |
      | ITEM_TWO | ASSOCIATION_ONE |

  Scenario: It should successfully publish an activity inside project and the plugin version should be resolved
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_FOUR" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_FOUR" for activity "LESSON_TWO"
    When "Alice" publishes "UNIT_ONE" activity from a project and "true" resolve plugin version
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" lists deployments for the cohort
    Then "Alice" verifies that the plugin versions are resolved to
      | UNIT_ONE | 1.2.0 |

  Scenario: It should successfully publish an activity with configuration data
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has saved configuration for "UNIT_ONE" activity with config
    """
    {"title": "Citrus Test - Activity Config"}
    """
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has saved configuration for "SCREEN_ONE" interactive with config
    """
    {"title": "Citrus Test - Interactive Config", "selection":8, "options":{"foo":"bar"}}
    """
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has saved configuration for "COMPONENT_ONE" component with config
    """
    {
        "title": "Flashcards",
        "description": "A plugin that allows learner to review a series of terms and their respective definitions, in a dual-sided card format",
        "cards": [
            {
                "label": "Card 1",
                "front-image": "urn:aero:43d54bf0-e7f4-11ea-9833-a9ab9362acd0",
                "front-text": "<p dir=\"ltr\">Front text list 01</p>",
                "back-text": "<p dir=\"ltr\">Back text list 01</p>",
                "back-image": "urn:aero:5cbf7870-e7f4-11ea-a2c1-7b220f226311"
            },
            {
                "label": "Card 2",
                "front-image": "urn:external:73bc7af0-e7f4-11ea-9833-a9ab9362acd0",
                "front-text": "<p dir=\"ltr\">Sydney harbour</p>",
                "back-text": "<p dir=\"auto\">Opera house</p>",
                "back-image": "urn:external:9ce34d00-e7f4-11ea-9833-a9ab9362acd0"
            }
        ]
    }
    """
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: It should successfully publish an activity and will create CSG Index
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has saved configuration for "LESSON_ONE" activity
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has saved configuration for "LESSON_TWO" activity
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_FOUR" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_FOUR" for activity "LESSON_TWO"
    And "Alice" has saved configuration for "UNIT_ONE" activity
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: It should successfully publish an activity with selected theme for a root activity
    When "Alice" creates theme "theme_one" for workspace "one"
    Then "theme_one" is successfully created
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    Then "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    And "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    And "Alice" has updated the config for activity "UNIT_ONE" with
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_FOUR" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has created a "COMPONENT_ONE" component for the "LESSON_ONE" activity
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_FOUR" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_TWO" interactive
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    And "Alice" has created scenario "SCENARIO_FOUR" for activity "LESSON_TWO"
    When "Alice" publishes "UNIT_ONE" activity from a project
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
