Feature: Pathway creation

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: User should be able to create a pathway for activity
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a LINEAR pathway for the activity
    Then the LINEAR pathway is successfully created

  Scenario: User is able to create pathway with supplied id
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a "LINEAR" pathway for activity "activity" with a supplied id
    Then the LINEAR pathway is successfully created with the supplied id

  Scenario: User can not create pathway with same id if id already exists
    Given "Alice" creates a "LINEAR" pathway for activity "activity" with a supplied id
    When "Alice" creates a "LINEAR" pathway for activity "activity" with the same supplied id as before
    Then the pathway is not created due to conflict

  Scenario: User should get an exception if activity doesn't exist
    When "Alice" creates a LINEAR pathway for random activity
    Then the pathway creation fails due to code 400 and message "invalid activityId"

  Scenario: User should be able to create a pathway and add an activity to it
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR" pathway
    Then the "LINEAR" pathway should have "LESSON_ONE" activity as child

  Scenario: User should not be able to create pathway if he doesn't have Contributor permissions on workspace
    Given "Alice" has created activity "activity" inside project "TRO"
    And a workspace account "Bob" is created
    When "Bob" creates a LINEAR pathway for the activity
    Then the pathway creation fails due to code 401 and message "Unauthorized: Unauthorized permission level"
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Bob" creates a LINEAR pathway for the activity
    Then the LINEAR pathway is successfully created

  Scenario: A user should be able to create a pathway with a config parameter
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    When "Alice" creates a "LINEAR" pathway for activity "UNIT_ONE" with config
    """
      {"foo":"bar"}
    """
    Then the "LINEAR_ONE" pathway is succesfully created and has config
    """
      {"foo":"bar"}
    """

  Scenario: A user should be able to replace config for an existing pathway
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for activity "UNIT_ONE" with config
    """
      {"foo":"bar"}
    """
    When "Alice" replace config for "LINEAR_ONE" pathway with
    """
      {"fuzz":"buzz"}
    """
    Then the "LINEAR_ONE" pathway config are successfully replaced with
    """
      {"fuzz":"buzz"}
    """
    When "Alice" fetches the "LINEAR_ONE" pathway
    Then "LINEAR_ONE" pathway is returned with config
    """
      {"fuzz":"buzz"}
    """

  Scenario: A user should be able to create a GRAPH pathway
    Given "Alice" has created activity "COURSE" inside project "TRO"
    When "Alice" creates a "GRAPH" pathway for the "COURSE" activity
    Then a "GRAPH" pathway named "PATHWAY_ONE" is successfully created

  Scenario: User should be able to create a linear pathway for activity with preload pathway
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a "LINEAR" pathway for the activity with preload pathway "ALL"
    Then the LINEAR pathway is successfully created

  Scenario: User is able to create linear pathway with supplied id with preload pathway
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a "LINEAR" pathway for activity "activity" with a supplied id and preload pathway "FIRST"
    Then the LINEAR pathway is successfully created with the supplied id

  Scenario: User is able to create graph pathway with supplied id with preload pathway
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a "GRAPH" pathway for activity "activity" with a supplied id and preload pathway "FIRST"
    Then the GRAPH pathway is successfully created with the supplied id
