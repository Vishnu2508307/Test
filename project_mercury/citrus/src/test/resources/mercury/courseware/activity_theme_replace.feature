Feature: Replace activity theme config

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "activity" inside project "TRO"

  Scenario: It should allow a workspace user to update the activity theme
    When "Alice" replace the activity "activity" theme with
     | color | blue |
    Then the activity theme is replaced successfully
    When she fetches the activity
    Then the "activity" activity is successfully fetched
