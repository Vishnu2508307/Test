Feature: Move components from one element to other

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And a workspace account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"

  Scenario: A user should be able to move an interactive component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR" pathway
    And "Alice" has created a component for the "SCREEN_ONE" "interactive"
    When "Alice" moves the "SCREEN_ONE" component to "SCREEN_TWO" "interactive"
    Then the "SCREEN_ONE" components are moved successfully
    And the "SCREEN_ONE" interactive does not contain the component
    When "Alice" fetches the "SCREEN_TWO" interactive
    And "SCREEN_TWO" interactive has 1 components

  Scenario: A user should be able to move an activity component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    Given "Alice" has created activity "UNIT2" inside project "TRO"
    And "Alice" has created a component for the "UNIT" "activity"
    When "Alice" fetches the "UNIT" activity
    Then the "UNIT" "activity" activity has 1 component and 0 pathway as children
    When "Alice" moves the "UNIT" component to "UNIT2" "activity"
    Then the "UNIT" components are moved successfully
    When "Alice" fetches the "UNIT" activity
    Then the "UNIT" activity has 0 component and 0 pathway as children
    When "Alice" fetches the "UNIT2" activity
    Then the "UNIT2" activity has 1 component and 0 pathway as children

    #Multi components move
  Scenario: A user should be able to move the components between two screens
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    And "Alice" has created a component "COMPONENT_TWO" for the "SCREEN_ONE" "interactive"
    When "Alice" moves "SCREEN_ONE" components to "SCREEN_TWO" "interactive"
      | COMPONENT_ONE |
      | COMPONENT_TWO |
    Then the "SCREEN_ONE" components are moved successfully
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_ONE"
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_TWO"
    When "Alice" fetches the "SCREEN_TWO" interactive
    And "SCREEN_TWO" interactive has 2 components

  Scenario: A user should be able to move the components from interactive to activity
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a "UNIT_ONE" activity for the "LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    And "Alice" has created a component "COMPONENT_TWO" for the "SCREEN_ONE" "interactive"
    When "Alice" moves "SCREEN_ONE" components to "UNIT_ONE" "activity"
      | COMPONENT_ONE |
      | COMPONENT_TWO |
    Then the "SCREEN_ONE" components are moved successfully
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_ONE"
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_TWO"
    When "Alice" fetches the "UNIT_ONE" activity
    Then the "UNIT_ONE" activity has 2 component and 0 pathway as children

  Scenario: A user with reviewer permission level should not be able to move the component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    And "Alice" has created a component "COMPONENT_TWO" for the "SCREEN_ONE" "interactive"
    When "Bob" moves "SCREEN_ONE" components to "SCREEN_TWO" "interactive"
      | COMPONENT_ONE |
      | COMPONENT_TWO |
    Then "Bob" not able to move components due to missing permission level