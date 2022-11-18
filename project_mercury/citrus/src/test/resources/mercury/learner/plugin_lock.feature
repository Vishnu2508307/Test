Feature: It should allow a plugin version to be locked

  Background:
    Given a workspace account "Alice" is created
    And an ies account "Bob" is provisioned
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "UNIT_ONE" student scope
    When "Alice" publishes "UNIT_ONE" activity from a project and "true" resolve plugin version
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: It should allow student to set scope before and after a plugin version update
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"A", "options":{"foo":"bar"}}
    """
    Then the student scope is successfully set with data
    """
    {"selection":"A", "options":{"foo":"bar"}}
    """
    Given "Alice" has changed the schema and published course plugin
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"B", "options":{"foo":"bar"}}
    """
    Then the student scope is successfully set with data
    """
    {"selection":"B", "options":{"foo":"bar"}}
    """
