Feature: Fetch learner selected theme

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: A user can publish courseware and fetch selected theme payload for an activity
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    When "Alice" creates theme variant "Normal_ONE" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_one" is created successfully
    When "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    When "Alice" publishes "UNIT_ONE" activity from a project
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" fetched selected theme for "UNIT_ONE" successfully

  Scenario: A user can publish courseware and not found selected theme payload for an activity if theme association deleted
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    When "Alice" creates theme variant "Normal_ONE" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_one" is created successfully
    When "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    And "Alice" deletes theme association for "ACTIVITY" "UNIT_ONE"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    When "Alice" publishes "UNIT_ONE" activity from a project
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" found no selected theme for "UNIT_ONE"

  Scenario: A user can publish courseware and not found selected theme payload for an activity if theme gets deleted
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    When "Alice" creates theme variant "Normal_ONE" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_one" is created successfully
    When "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    When "Alice" deletes theme "theme_one"
    Then "theme_one" is successfully deleted
    When "Alice" lists the themes
    Then the theme has empty list
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    When "Alice" publishes "UNIT_ONE" activity from a project
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" found no selected theme for "UNIT_ONE"

  Scenario: A user can publish courseware and fetch learner theme variant
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    When "Alice" creates theme variant "Normal_ONE" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_one" is created successfully
    When "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive
    And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_ONE" interactive
    When "Alice" publishes "UNIT_ONE" activity from a project
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    Then "Alice" get following learner theme variant info for "variant_one_theme_one" and theme "theme_one"
      | variantName | Main |
    Then "Alice" get following learner theme variant info for "variant_two_theme_one" and theme "theme_one"
      | variantName | Normal_ONE |
