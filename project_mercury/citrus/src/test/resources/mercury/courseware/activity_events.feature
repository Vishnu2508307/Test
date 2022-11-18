Feature: Event messages should be emitted on the tree structure on activity actions

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" is logged in to the default client
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"

  Scenario: It should emit events when an activity is created
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    Then "Bob" should receive an action "CREATED" message for the "LESSON" activity and "COURSE_LINEAR" parent pathway

  Scenario: It should emit events when an activity config is replaced
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has updated the config for activity "LESSON" with
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    Then "Bob" should receive an action "CONFIG_CHANGE" message for the "LESSON" activity

  Scenario: It should emit events when an activity description is replaced
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has added description "Some description" for "LESSON" "ACTIVITY"
    Then "Bob" should receive an action "DESCRIPTIVE_CHANGE" message for the "LESSON" activity

  Scenario: It should emit events when an activity theme config is replaced
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has updated the theme config for activity "LESSON" with
    """
    {"colo":"orange", "margin":"20"}
    """
    Then "Bob" should receive an action "THEME_CHANGE" message for the "LESSON" activity

  Scenario: It should emit events when an activity is deleted
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has deleted the "LESSON" activity for pathway "COURSE_LINEAR"
    Then "Bob" should receive an action "DELETED" message for the "LESSON" activity and "COURSE_LINEAR" parent pathway

  Scenario: Bob should not be notified when Alice changes the scenario after he unsubscribed
    Given "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When she has created scenarios for the "COURSE" activity
      | one | ACTIVITY_START |
    Then the activity scenario "one" creation should be notified to "Bob"
    When "Bob" unsubscribes to activity events for "COURSE" via a "Bob" client
    And she has created scenarios for the "COURSE" activity
      | two | ACTIVITY_EVALUATE |
    Then "Bob" should not be notified

  Scenario: It should broadcast content when an activity is duplicated
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" duplicates "LESSON" activity into "COURSE_LINEAR" pathway at position 1
    Then the "LESSON_COPY" activity has been successfully duplicated
    And "Bob" should receive an action "DUPLICATED" message for the "LESSON_COPY" activity and "COURSE_LINEAR" parent pathway

  Scenario: It should broadcast content when an annotation is created/updated/deleted
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created a courseware "classifying" annotation "one" through rtm for element "LESSON" of type "ACTIVITY" and rootElement "COURSE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then "Bob" should receive an action "ANNOTATION_CREATED" message for the "LESSON" activity
    When "Alice" has updated a courseware annotation "one" for element "LESSON" of type "ACTIVITY" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then "Bob" should receive an action "ANNOTATION_UPDATED" message for the "LESSON" activity
    When "Alice" has deleted annotation "one" for courseware element "LESSON" of type "ACTIVITY"
    Then "Bob" should receive an action "ANNOTATION_DELETED" message for the "LESSON" activity

  Scenario: It should broadcast content when an evaluable is set
    Given "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has created evaluable for "LESSON" "ACTIVITY" with evaluation mode "DEFAULT"
    Then "Bob" should receive an action "EVALUABLE_SET" message for the "LESSON" activity

  Scenario: It should broadcast content when an element is able to create or delete theme association
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    And "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "LESSON" activity for the "COURSE_LINEAR" pathway
    When "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    And "Alice" associate theme "theme_one" with "ACTIVITY" "COURSE"
    Then theme "theme_one" is successfully associated with activity "COURSE"
    Then "Bob" should receive an action "ELEMENT_THEME_CREATE" message for the "COURSE" activity
    When "Alice" delete theme association for "ACTIVITY" "COURSE"
    Then the theme is successfully deleted
    Then "Bob" should receive an action "ELEMENT_THEME_DELETE" message for the "COURSE" activity
