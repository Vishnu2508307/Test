Feature: Delete and restore a Component

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And a workspace account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"

  Scenario: A user should be able to delete an interactive component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a component for the "SCREEN_ONE" "interactive"
    When "Alice" deletes the "SCREEN_ONE" "interactive" component
    Then the "SCREEN_ONE" "interactive" component is successfully deleted
    And the "SCREEN_ONE" interactive does not contain the component

  Scenario: A user should be able to delete an activity component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a component for the "UNIT" "activity"
    When "Alice" fetches the "UNIT" activity
    Then the "UNIT" "activity" activity has 1 component and 0 pathway as children
    When "Alice" deletes the "UNIT" "activity" component
    Then the "UNIT" "activity" component is successfully deleted
    When "Alice" fetches the "UNIT" activity
    Then the "UNIT" activity has 0 component and 0 pathway as children

  Scenario: A user should not be able to fetch a deleted activity component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a component for the "UNIT" "activity"
    And "Alice" has deleted the "UNIT" "activity" component
    Then the component cannot be fetched anymore due to code 401 and message "Unauthorized: Unauthorized permission level"

  Scenario: A user should not be able to fetch a deleted interactive component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a component for the "SCREEN_ONE" "interactive"
    And "Alice" has deleted the "SCREEN_ONE" "interactive" component
    Then the component cannot be fetched anymore due to code 401 and message "Unauthorized: Unauthorized permission level"

    #Restore components
  Scenario: A user should be able to restore the deleted component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    When "Alice" deleted the "SCREEN_ONE" "interactive" component "COMPONENT_ONE"
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_ONE"
    And "Alice" has created a component "COMPONENT_TWO" for the "SCREEN_ONE" "interactive"
    When "Alice" deleted the "SCREEN_ONE" "interactive" component "COMPONENT_TWO"
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_TWO"
    Then "Alice" restored deleted components for the interactive "SCREEN_ONE"
      | COMPONENT_ONE |
      | COMPONENT_TWO |
    And the interactive "SCREEN_ONE" contains following components
      | COMPONENT_ONE |
      | COMPONENT_TWO |

  Scenario: A user with reviewer permission level should not be able to restore the deleted component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    When "Alice" deleted the "SCREEN_ONE" "interactive" component "COMPONENT_ONE"
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_ONE"
    And "Alice" has created a component "COMPONENT_TWO" for the "SCREEN_ONE" "interactive"
    When "Alice" deleted the "SCREEN_ONE" "interactive" component "COMPONENT_TWO"
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_TWO"
    When "Bob" restore deleted components for the interactive "SCREEN_ONE"
      | COMPONENT_ONE |
      | COMPONENT_TWO |
    Then "Bob" not able to restore components due to missing permission level

      #Multi component delete

  Scenario: A user should be able to delete an interactive components
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    And "Alice" has created a component "COMPONENT_TWO" for the "SCREEN_ONE" "interactive"
    Then "Alice" deleted the "SCREEN_ONE" "interactive" components
      | COMPONENT_ONE |
      | COMPONENT_TWO |
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_ONE"
    And the "SCREEN_ONE" interactive does not contain the components "COMPONENT_TWO"

  Scenario: A user should not be able to fetch a deleted interactive component
    Given "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR" pathway
    And "Alice" has created a component "COMPONENT_ONE" for the "SCREEN_ONE" "interactive"
    And "Alice" has created a component "COMPONENT_TWO" for the "SCREEN_ONE" "interactive"
    Then "Alice" deleted the "SCREEN_ONE" "interactive" components
      | COMPONENT_ONE |
      | COMPONENT_TWO |
    Then the component "COMPONENT_ONE" cannot be fetched anymore due to code 401 and message "Unauthorized: Unauthorized permission level"
