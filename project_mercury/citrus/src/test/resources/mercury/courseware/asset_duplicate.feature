Feature: Links to assets should be duplicated on APIC duplication

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has uploaded asset "assetUploadTest.jpg" as "Image" with visibility "GLOBAL" and metadata
      | config      | some config       |
      | description | asset description |
      | alfrescoPath | alfrescoAssetPath |
      | altText | alfrescoAltText |
      | longDescription | alfrescoLongDescription |

  Scenario: Activity assets should be duplicated on activity duplication
    Given "Alice" has added "Image" asset to "COURSE" activity
    When "Alice" duplicates "COURSE" to project "TRO"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "COURSE_COPY" activity payload contains "Image" asset

  Scenario: Pathway assets should be duplicated on activity duplication
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has added "Image" asset to "LINEAR" pathway
    When "Alice" duplicates "COURSE" to project "TRO"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway payload contains "Image" asset

  Scenario: Interactive and feedback assets should be duplicated on activity duplication
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a "FEEDBACK" feedback for the "SCREEN" interactive
    And "Alice" has added "Image" asset to "SCREEN" interactive
    And "Alice" has added "Image" asset to "FEEDBACK" feedback
    When "Alice" duplicates "COURSE" to project "TRO"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "Alice" fetches the "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway has walkable children
      | SCREEN_COPY |
    And "SCREEN_COPY" interactive payload contains "Image" asset
    And "Alice" fetches the "SCREEN_COPY" interactive
    Then "SCREEN_COPY" interactive has "FEEDBACK_COPY" feedback
    And "FEEDBACK_COPY" feedback payload contains "Image" asset

  Scenario: Component assets should be duplicated on activity duplication
    Given "Alice" has created a "PROGRESS" component for the "COURSE" activity
    And "Alice" has added "Image" asset to "PROGRESS" component
    When "Alice" duplicates "COURSE" to project "TRO"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "PROGRESS_COPY" component
    And "PROGRESS_COPY" component payload contains "Image" asset

#   A new asset id will be generated if a course and destination project are not in the same project and new duplicate flow is on
  Scenario: Activity assets should be duplicated with a new asset id on activity duplication while new duplicate flow is on
    Given "Alice" has added "Image" asset to "COURSE" activity
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "COURSE_COPY" activity payload contains "Image" asset with a new asset id

  Scenario: Pathway assets should be duplicated with a new asset id on activity duplication while new duplicate flow is on
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has added "Image" asset to "LINEAR" pathway
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway payload contains "Image" asset with a new asset id

  Scenario: Interactive and feedback assets should be duplicated with new asset ids on activity duplication while new duplicate flow is on
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a "FEEDBACK" feedback for the "SCREEN" interactive
    And "Alice" has added "Image" asset to "SCREEN" interactive
    And "Alice" has added "Image" asset to "FEEDBACK" feedback
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "Alice" fetches the "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway has walkable children
      | SCREEN_COPY |
    And "SCREEN_COPY" interactive payload contains "Image" asset with a new asset id
    And "Alice" fetches the "SCREEN_COPY" interactive
    Then "SCREEN_COPY" interactive has "FEEDBACK_COPY" feedback
    And "FEEDBACK_COPY" feedback payload contains "Image" asset with a new asset id

  Scenario: Component assets should be duplicated with a new asset id on activity duplication while new duplicate flow is on
    Given "Alice" has created a "PROGRESS" component for the "COURSE" activity
    And "Alice" has added "Image" asset to "PROGRESS" component
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "PROGRESS_COPY" component
    And "PROGRESS_COPY" component payload contains "Image" asset with a new asset id

  Scenario: Activity assets should be duplicated with a new asset id and config with new asset urn while new duplicate flow is on
    Given "Alice" has added "Image" asset to "COURSE" activity
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" saves "Image" asset configuration for the "COURSE" activity
    """
    {"title":"Demo Activity config", "image":"AssetURN"}
    """
    Then the activity configuration is successfully saved
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "COURSE_COPY" activity payload contains "Image" asset with a new asset id and config with new asset urn

  Scenario: Interactive and feedback assets should be duplicated with new asset ids and config with new asset urn while new duplicate flow is on
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a "FEEDBACK" feedback for the "SCREEN" interactive
    And "Alice" has added "Image" asset to "SCREEN" interactive
    And "Alice" has added "Image" asset to "FEEDBACK" feedback
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" saves "Image" asset configuration for the "SCREEN" interactive
    """
    {"title":"Demo Interactive config", "image":"AssetURN"}
    """
    Then the "SCREEN" interactive configuration is successfully saved
    When "Alice" saves "Image" asset configuration for the "FEEDBACK" feedback
    """
    {"title":"Demo Feedback config", "image":"AssetURN"}
    """
    Then the "FEEDBACK" feedback configuration is successfully saved
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "Alice" fetches the "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway has walkable children
      | SCREEN_COPY |
    And "SCREEN_COPY" interactive payload contains "Image" asset with a new asset id and config with new asset urn
    And "Alice" fetches the "SCREEN_COPY" interactive
    Then "SCREEN_COPY" interactive has "FEEDBACK_COPY" feedback
    And "FEEDBACK_COPY" feedback payload contains "Image" asset with a new asset id and config with new asset urn

  Scenario: Component assets should be duplicated with a new asset id and config with new asset urn while new duplicate flow is on
    Given "Alice" has created a "PROGRESS" component for the "COURSE" activity
    And "Alice" has added "Image" asset to "PROGRESS" component
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" saves "Image" asset configuration for the "PROGRESS" component
    """
    {"title":"Demo Component config", "image":"AssetURN"}
    """
    Then the "PROGRESS" component configuration is successfully saved
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "PROGRESS_COPY" component
    And "PROGRESS_COPY" component payload contains "Image" asset with a new asset id and config with new asset urn

#   No new asset id will be generated even a course and the destination project are not in the same project but new duplicate flow is off
  Scenario: Activity assets should be duplicated on activity duplication while new duplicate flow is off
    Given "Alice" has added "Image" asset to "COURSE" activity
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "off"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "COURSE_COPY" activity payload contains "Image" asset

  Scenario: Pathway assets should be duplicated on activity duplication while new duplicate flow is off
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has added "Image" asset to "LINEAR" pathway
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "off"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway payload contains "Image" asset

  Scenario: Interactive and feedback assets should be duplicated on activity duplication while new duplicate flow is off
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a "FEEDBACK" feedback for the "SCREEN" interactive
    And "Alice" has added "Image" asset to "SCREEN" interactive
    And "Alice" has added "Image" asset to "FEEDBACK" feedback
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "off"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "Alice" fetches the "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway has walkable children
      | SCREEN_COPY |
    And "SCREEN_COPY" interactive payload contains "Image" asset
    And "Alice" fetches the "SCREEN_COPY" interactive
    Then "SCREEN_COPY" interactive has "FEEDBACK_COPY" feedback
    And "FEEDBACK_COPY" feedback payload contains "Image" asset

  Scenario: Component assets should be duplicated on activity duplication while new duplicate flow is off
    Given "Alice" has created a "PROGRESS" component for the "COURSE" activity
    And "Alice" has added "Image" asset to "PROGRESS" component
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    When "Alice" duplicates "COURSE" to project "ACCOUNTING" while new duplicate flow is "off"
    Then the new copy "COURSE_COPY" is successfully created inside project "ACCOUNTING"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "PROGRESS_COPY" component
    And "PROGRESS_COPY" component payload contains "Image" asset

#  No new asset id will be generated if a course and the destination project are in the same project even new duplicate flow is on
  Scenario: Activity assets should be duplicated on activity duplication while new duplicate flow is on
    Given "Alice" has added "Image" asset to "COURSE" activity
    When "Alice" duplicates "COURSE" to project "TRO" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "COURSE_COPY" activity payload contains "Image" asset

  Scenario: Pathway assets should be duplicated on activity duplication while new duplicate flow is on
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has added "Image" asset to "LINEAR" pathway
    When "Alice" duplicates "COURSE" to project "TRO" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway payload contains "Image" asset

  Scenario: Interactive and feedback assets should be duplicated on activity duplication while new duplicate flow is on
    Given "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a "FEEDBACK" feedback for the "SCREEN" interactive
    And "Alice" has added "Image" asset to "SCREEN" interactive
    And "Alice" has added "Image" asset to "FEEDBACK" feedback
    When "Alice" duplicates "COURSE" to project "TRO" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "LINEAR_COPY" pathway
    And "Alice" fetches the "LINEAR_COPY" pathway
    And "LINEAR_COPY" pathway has walkable children
      | SCREEN_COPY |
    And "SCREEN_COPY" interactive payload contains "Image" asset
    And "Alice" fetches the "SCREEN_COPY" interactive
    Then "SCREEN_COPY" interactive has "FEEDBACK_COPY" feedback
    And "FEEDBACK_COPY" feedback payload contains "Image" asset

  Scenario: Component assets should be duplicated on activity duplication while new duplicate flow is on
    Given "Alice" has created a "PROGRESS" component for the "COURSE" activity
    And "Alice" has added "Image" asset to "PROGRESS" component
    When "Alice" duplicates "COURSE" to project "TRO" while new duplicate flow is "on"
    Then the new copy "COURSE_COPY" is successfully created inside project "TRO"
    And "Alice" fetches the "COURSE_COPY" activity
    And "COURSE_COPY" activity has a "PROGRESS_COPY" component
    And "PROGRESS_COPY" component payload contains "Image" asset