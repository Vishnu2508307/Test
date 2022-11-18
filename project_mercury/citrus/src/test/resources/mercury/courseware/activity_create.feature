Feature: Activity creation

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: user should not be able to create an activity if plugin doesn't exist
    When "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    When "Alice" creates a "LESSON" activity for the "LINEAR" pathway with some random pluginId
    Then the activity creation fails with message that version doesn't exist

  Scenario: User should be able to save configuration for an activity and fetch it
    When "Alice" has created activity "UNIT" inside project "TRO"
    And she saves configuration for the "UNIT" activity
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    Then the activity configuration is successfully saved
    When she fetches the "UNIT" activity
    Then the "UNIT" activity is successfully fetched

    # TODO: Theme changes

    # TODO Scenario changes

  Scenario: User should be able to create an activity as child of an existing pathway
    When "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    When "Alice" creates a "LESSON" activity for the "LINEAR" pathway
    Then the "LESSON" activity for the "LINEAR" pathway is successfully created

  Scenario: User should be able to see children components, pathway for an activity
    When "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has added a "PROGRESS" component to the "UNIT" activity
    And "Alice" has created a "LINEAR_UNIT" pathway for the "UNIT" activity
    When "Alice" fetches the "UNIT" activity
    Then the "UNIT" activity has 1 component and 1 pathway as children
#
  Scenario: User should be able to create an Activity with configuration, component with config
    When "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    Then "Alice" has created an "UNIT" "activity" component with config
    """
    {"title":"component"}
    """

  Scenario: User should be able to fetch theme information for an activity
    When "Alice" creates theme "theme_one" for workspace "one"
    Then "theme_one" is successfully created
    When "Alice" has created activity "UNIT" inside project "TRO"
    And she saves configuration for the "UNIT" activity
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    Then the activity configuration is successfully saved
    When "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    And "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT"
    Then theme "theme_one" is successfully associated with activity "UNIT"
    When she fetches the "UNIT" activity
    Then the "UNIT" activity is successfully fetched with theme "theme_one" information

  Scenario: User should be able to create a project activity by providing activity id
    When "Alice" creates activity "UNIT" inside project "TRO" with id
    Then activity "UNIT" is successfully created inside project "TRO" with provided id

  Scenario: User can not create project activity with same id if id already exists
    When "Alice" creates activity "UNIT" inside project "TRO" with id
    Then activity "UNIT" is successfully created inside project "TRO" with provided id
    When "Alice" creates activity "LESSON" inside project "TRO" with the same id
    Then the project activity is not created due to conflict

  Scenario: User should be able to create an activity by providing activity id
    When "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    When "Alice" creates a "LESSON" activity for the "LINEAR" pathway with id
    Then activity "LESSON" is successfully created with provided id

  Scenario: User can not create child activity with same id if id already exists
    When "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    When "Alice" creates a "LESSON" activity for the "LINEAR" pathway with id
    Then activity "LESSON" is successfully created with provided id
    When "Alice" creates a "LESSON" activity for the "LINEAR" pathway with the same id
    Then the activity is not created due to conflict
