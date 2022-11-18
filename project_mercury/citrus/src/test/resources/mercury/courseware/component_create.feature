Feature: Creation a Component

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: The user should get an error message on component creation if an interactive does not exist
    When "Alice" creates a component for random interactive
    Then interactive component creation fails with message "invalid interactive" and code 400

  Scenario: User should be able to create a component for an interactive and save configuration
    Given "Alice" has created activity "activity" inside project "TRO"
    And "Alice" has created a pathway
    And "Alice" has created an interactive
    When "Alice" creates a component for the "interactive"
    Then the "interactive" component is successfully created
    When "Alice" saves configuration for the component
    """
    {"title":"Demo Component", "desc":"some nice component"}
    """
    Then the component configuration is successfully saved

  Scenario: User should be able to create a component for an activity and save the configuration
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a component for the "activity"
    Then the "activity" component is successfully created
    When "Alice" saves configuration for the component
    """
    {"title":"Demo Component", "desc":"some nice component"}
    """
    Then the component configuration is successfully saved

  Scenario: User should be able to create a component for an activity with supplied id
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a component for "activity" with a supplied id
    Then the "activity" component is successfully created with the supplied id

  Scenario: User should not be able to create a component for an activity with same id if id already exists
    Given "Alice" has created activity "activity" inside project "TRO"
    When "Alice" creates a component for "activity" with a supplied id
    Then the "activity" component is successfully created with the supplied id
    When "Alice" creates a component for "activity" with the same supplied id as before
    Then the "activity" component is not created due to conflict


  Scenario: User should be able to create a component for an interactive with supplied id
    Given "Alice" has created activity "activity" inside project "TRO"
    And "Alice" has created a pathway
    And "Alice" has created an interactive
    When "Alice" creates a component for "interactive" with a supplied id
    Then the "interactive" component is successfully created with the supplied id

  Scenario: User should not be able to create a component for an interactive with same id if id already exists
    Given "Alice" has created activity "activity" inside project "TRO"
    And "Alice" has created a pathway
    And "Alice" has created an interactive
    When "Alice" creates a component for "interactive" with a supplied id
    Then the "interactive" component is successfully created with the supplied id
    When "Alice" creates a component for "interactive" with the same supplied id as before
    Then the "interactive" component is not created due to conflict
