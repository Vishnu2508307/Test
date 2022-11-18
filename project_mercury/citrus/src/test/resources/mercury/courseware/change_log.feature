Feature: Project and courseware element changelogs feature

  Background:
    Given an account "Alice" is created
    Given "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: It should list all courseware change log for project
    Given "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    Then "Alice" can save config for "ACTIVITY_ONE" activity successfully
    Then "Alice" can list the following courseware changelog  for project "TRO"
      | 1__ACTIVITY_ONE | CONFIG_CHANGE |
      | 2__ACTIVITY_ONE | CREATED       |

  Scenario: It should list all courseware change log for element
    Given "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    Then "Alice" can save config for "ACTIVITY_ONE" activity successfully
    Then "Alice" can list the following courseware changelog for element "ACTIVITY_ONE"
      | 1__ACTIVITY_ONE | CONFIG_CHANGE |
      | 2__ACTIVITY_ONE | CREATED       |

  Scenario: It should list all courseware change log for a project with respect to activity creation and deletion.
    Given "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    When "Alice" deletes activity "ACTIVITY_ONE" from project "TRO"
    Then activity "ACTIVITY_ONE" is successfully deleted from project "TRO"
    Then "Alice" can list the following courseware changelog  for project "TRO"
      | 1__ACTIVITY_ONE | DELETED |
      | 2__ACTIVITY_ONE | CREATED |

  Scenario: User should be able to see created interactive in changelog
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    When "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Then "Alice" can list the following courseware changelog for element "SCREEN_ONE"
      | 1__SCREEN_ONE | CREATED |

  Scenario: User should be able to see deleted interactive in changelog
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    When "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Alice" deletes the "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Then the "SCREEN_ONE" interactive is deleted
    Then "Alice" can list the following courseware changelog for element "UNIT_ONE"
      | 1__SCREEN_ONE | DELETED |
      | 2__SCREEN_ONE | CREATED |
      | 3__LINEAR_ONE | CREATED |
      | 4__UNIT_ONE   | CREATED |

  Scenario: User should be able to see created feedback in changelog
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    When "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_ONE" interactive
    Then "Alice" can list the following courseware changelog for element "SCREEN_ONE"
      | 1__FEEDBACK_ONE | CREATED |
      | 2__SCREEN_ONE   | CREATED |

  Scenario: User should be able to see deleted feedback in changelog
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    When "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_ONE" interactive
    And "Alice" can delete "FEEDBACK_ONE" feedback for "SCREEN_ONE" interactive successfully
    Then "Alice" can list the following courseware changelog for element "UNIT_ONE"
      | 1__FEEDBACK_ONE | DELETED |
      | 2__FEEDBACK_ONE | CREATED |
      | 3__SCREEN_ONE   | CREATED |
      | 4__LINEAR_ONE   | CREATED |
      | 5__UNIT_ONE     | CREATED |

  Scenario: User should be able to see annotation created/updated/deleted in changelog
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Alice" creates a courseware "identifying" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    When "Alice" updates a courseware annotation "one" with motivation "identifying" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then the courseware annotation "one" is updated successfully
    When "Alice" deletes annotation "one" of courseware element "SCREEN_ONE" of type "INTERACTIVE"
    Then the courseware annotation "one" is deleted successfully
    And "Alice" can list the following courseware changelog for element "UNIT_ONE"
      | 1__SCREEN_ONE | ANNOTATION_DELETED |
      | 2__SCREEN_ONE | ANNOTATION_UPDATED |
      | 3__SCREEN_ONE | ANNOTATION_CREATED |
      | 4__SCREEN_ONE | CREATED |
      | 5__LINEAR_ONE | CREATED |
      | 6__UNIT_ONE   | CREATED |

  Scenario: User should be able to see evaluable created in changelog
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Alice" has created evaluable for "SCREEN_ONE" "INTERACTIVE" with evaluation mode "COMBINED"
    Then "Alice" can list the following courseware changelog for element "UNIT_ONE"
      | 1__SCREEN_ONE | EVALUABLE_SET |
      | 2__SCREEN_ONE | CREATED |
      | 3__LINEAR_ONE | CREATED |
      | 4__UNIT_ONE   | CREATED |

  Scenario: User should be able to see element theme created and deleted changelog
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    Then "Alice" can list the following courseware changelog for element "UNIT_ONE"
      | 1__UNIT_ONE | ELEMENT_THEME_CREATE |
      | 2__UNIT_ONE | CREATED              |
    When "Alice" delete theme association for "ACTIVITY" "UNIT_ONE"
    Then the theme is successfully deleted
    Then "Alice" can list the following courseware changelog for element "UNIT_ONE"
      | 1__UNIT_ONE | ELEMENT_THEME_DELETE |
      | 2__UNIT_ONE | ELEMENT_THEME_CREATE |
      | 3__UNIT_ONE | CREATED              |

