Feature: Add/remove assets for courseware elements

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has uploaded special "Image" asset once
    And "Alice" has uploaded special "Image2" asset once
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"

  Scenario: User should be able to add/remove asset for activity and event should be broadcasted
    Given "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has added "Image" asset to "COURSE" activity
    Then "COURSE" activity payload contains "Image" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "COURSE" activity
    When "Alice" has added "Image2" asset to "COURSE" activity
    Then "COURSE" activity payload contains "Image2" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "COURSE" activity
    When "Alice" has removed asset from "COURSE" activity
      | Image |
      | Image2 |
    Then "COURSE" activity payload does not contain assets
    And "Bob" should receive an action "ASSETS_REMOVED" message for the "COURSE" activity

  Scenario: User should be able to add/remove asset for pathway and event should be broadcasted
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has added "Image" asset to "LINEAR" pathway
    Then "LINEAR" pathway payload contains "Image" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "LINEAR" pathway
    When "Alice" has added "Image2" asset to "LINEAR" pathway
    Then "LINEAR" pathway payload contains "Image2" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "LINEAR" pathway
    When "Alice" has removed asset from "LINEAR" pathway
      | Image |
      | Image2 |
    Then "LINEAR" pathway payload does not contain assets
    And "Bob" should receive an action "ASSETS_REMOVED" message for the "LINEAR" pathway

  Scenario: User should be able to add/remove asset for interactive and event should be broadcasted
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has added "Image" asset to "SCREEN" interactive
    Then "SCREEN" interactive payload contains "Image" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "SCREEN" interactive
    When "Alice" has added "Image2" asset to "SCREEN" interactive
    Then "SCREEN" interactive payload contains "Image2" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "SCREEN" interactive
    When "Alice" has removed asset from "SCREEN" interactive
      | Image |
      | Image2 |
    Then "SCREEN" interactive payload does not contain assets
    And "Bob" should receive an action "ASSETS_REMOVED" message for the "SCREEN" interactive

  Scenario: User should be able to add/remove asset for component and event should be broadcasted
    Given "Alice" has created a "PROGRESS" component for the "COURSE" activity
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has added "Image" asset to "PROGRESS" component
    Then "PROGRESS" component payload contains "Image" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "PROGRESS" component
    When "Alice" has added "Image2" asset to "PROGRESS" component
    Then "PROGRESS" component payload contains "Image2" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "PROGRESS" component
    When "Alice" has removed asset from "PROGRESS" component
      | Image |
      | Image2 |
    Then "PROGRESS" component payload does not contain assets
    And "Bob" should receive an action "ASSETS_REMOVED" message for the "PROGRESS" component

  Scenario: User should be able to add/remove asset for feedback and event should be broadcasted
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a "FEEDBACK" feedback for the "SCREEN" interactive
    And "Bob" subscribes to "activity" events for "COURSE" via a "Bob" client
    When "Alice" has added "Image" asset to "FEEDBACK" feedback
    Then "FEEDBACK" feedback payload contains "Image" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "FEEDBACK" feedback
    When "Alice" has added "Image2" asset to "FEEDBACK" feedback
    Then "FEEDBACK" feedback payload contains "Image2" asset
    And "Bob" should receive an action "ASSET_ADDED" message for the "FEEDBACK" feedback
    When "Alice" has removed asset from "FEEDBACK" feedback
      | Image |
      | Image2 |
    Then "FEEDBACK" feedback payload does not contain assets
    And "Bob" should receive an action "ASSETS_REMOVED" message for the "FEEDBACK" feedback
