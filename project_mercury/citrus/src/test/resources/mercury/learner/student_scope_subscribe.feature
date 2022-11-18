Feature: Subscription to student scope changes

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
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: User should be able to subscribe and unsubscribe to scope changes and get messages
    Given "Bob" has subscribed to student scope in deployment "DEPLOYMENT_ONE"
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"A", "options":{"foo":"bar"}}
    """
    Then "Bob" gets student scope broadcasts for "DEPLOYMENT_ONE" and student scope "UNIT_ONE" set by "SCREEN_ONE" with data
    """
    {"selection":"A", "options":{"foo":"bar"}}
    """
    When "Bob" sets "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"B", "options":{"foo":"bar"}}
    """
    Then "Bob" gets student scope broadcasts for "DEPLOYMENT_ONE" and student scope "SCREEN_ONE" set by "SCREEN_ONE" with data
    """
    {"selection":"B", "options":{"foo":"bar"}}
    """
    When "Bob" un-subscribes from student scope in deployment "DEPLOYMENT_ONE"
    And "Bob" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"B", "options":{"foo":"bar"}}
    """
    Then "Bob" should not receive a broadcast
