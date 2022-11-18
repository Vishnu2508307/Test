Feature: Creation an Interactive

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: The user should get an error message on interactive creation if pathway does not exist
    When "Alice" creates an interactive for random pathway
    Then interactive creation fails with message "invalid pathway" and code 400

  Scenario: User should be able to create an Interactive with configuration, components and feedbacks and successfully fetch it
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a pathway for the "UNIT_ONE" activity
    When "Alice" creates an interactive for this pathway
    Then the interactive is successfully created
    When "Alice" saves configuration for the interactive
    """
    {"title":"Demo Screen", "desc":"Nice screen"}
    """
    Then the interactive configuration is successfully saved
    When "Alice" creates a component for the "interactive"
    Then the "interactive" component is successfully created
    And "Alice" has created a feedback for the interactive
    And "Alice" fetches the "interactive" interactive
    Then the interactive payload is successfully fetched

  Scenario: User should be able to create an Interactive with configuration, component with config
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a pathway for the "UNIT_ONE" activity
    And "Alice" has created an interactive
    Then "Alice" has created an "interactive" component with config
    """
    {"title":"component"}
    """

  Scenario: The latest config should be fetched
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a pathway for the "UNIT_ONE" activity
    And "Alice" has created an interactive
    And "Alice" has saved configuration "config1" for the interactive
    And "Alice" has saved configuration "config2" for the interactive
    When "Alice" fetches the "interactive" interactive
    Then the interactive payload is successfully fetched

  Scenario: User should be able to provide configuration during interactive creation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a pathway for the "UNIT_ONE" activity
    When "Alice" creates an interactive for this pathway with config '{"config":"my awesome config"}'
    Then the interactive is successfully created with config '{"config":"my awesome config"}'

  Scenario: User should be able to create an interactive by providing interactive id
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a pathway for the "UNIT_ONE" activity
    When "Alice" creates an interactive for this pathway with provided id
    Then the interactive is successfully created with provided id

  Scenario: User can not create project activity with same id if id already exists
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a pathway for the "UNIT_ONE" activity
    When "Alice" creates an interactive for this pathway with provided id
    Then the interactive is successfully created with provided id
    When "Alice" creates an interactive for this pathway with the same id
    Then the interactive is not created due to conflict
