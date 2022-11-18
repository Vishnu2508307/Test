Feature: Diff sync feature

  Background:
    Given a workspace account "Alice" is created
    And "Alice" is logged via a "Alice" client
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client

  Scenario: Users can diff sync on activity config on same server
    When "Alice" has created activity "UNIT" inside project "TRO"
    And she saves configuration for the "UNIT" activity
     """
    {"Name":"Hello"}
    """
    Then the activity configuration is successfully saved
    And "Alice" initiated diff sync on entityType "ACTIVITY_CONFIG" and entityId "UNIT"
    And "Bob" initiated diff sync on entityType "ACTIVITY_CONFIG" and entityId "UNIT"
    And "Alice" initiates diff sync patch on entityType "ACTIVITY_CONFIG" and entityId "UNIT" with config "Config2"
    Then "Alice" should receive ACK notification with type "diffsync.ack"
    And "Alice" should receive PATCH notification with type "diffsync.patch"
    Then "Alice" diff sync patched is successfully
    And "Bob" should receive PATCH notification with type "diffsync.patch"
    And "Bob" initiates diff sync patch on entityType "ACTIVITY_CONFIG" and entityId "UNIT" with config "Config2"
    Then "Bob" should receive ACK notification with type "diffsync.ack"
    And "Bob" should receive PATCH notification with type "diffsync.patch"
    Then "Bob" diff sync patched is successfully
    And "Alice" should receive PATCH notification with type "diffsync.patch"
    Then "Alice" ended the diff sync on entityType "ACTIVITY_CONFIG" and entityId "UNIT"

  Scenario: Users can diff sync each other on interactive config
    When "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has updated the "SCREEN" interactive config with "hello"
    When "Alice" initiated diff sync on entityType "INTERACTIVE_CONFIG" and entityId "SCREEN"
    And "Bob" initiated diff sync on entityType "INTERACTIVE_CONFIG" and entityId "SCREEN"
    And "Alice" initiates diff sync patch on entityType "INTERACTIVE_CONFIG" and entityId "SCREEN" with config "world"
    Then "Alice" should receive ACK notification with type "diffsync.ack"
    And "Alice" should receive PATCH notification with type "diffsync.patch"
    Then "Alice" diff sync patched is successfully
    And "Bob" should receive PATCH notification with type "diffsync.patch"






