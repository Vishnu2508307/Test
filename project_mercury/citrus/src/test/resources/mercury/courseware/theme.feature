Feature: Create update and delete a theme

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And a workspace account "Bob" is created
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: A workspace user with owner permission level should be able to create and update theme
    When "Alice" creates theme "theme_one" for workspace "one"
    Then "theme_one" is successfully created
    When "Alice" updates theme "theme_one"
    Then "theme_one" is successfully updated

  Scenario: A workspace user with reviewer permission level should not be able to create a theme
    When "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Bob" creates theme "theme_one" for workspace "one"
    Then the theme is not created due to missing permission level

  Scenario: A workspace user with contributor or higher permission level over the workspace should be able to create and update theme
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" creates theme "theme_two" for workspace "one"
    Then "theme_two" is successfully created
    When "Bob" updates theme "theme_two" with name "theme_new"
    Then "theme_two" is successfully updated

  Scenario: A user with contributor or higher permission level over the theme should be able to update theme
    When "Alice" created theme "theme_two" for workspace "one"
    When "Alice" granted "Bob" with "CONTRIBUTOR" permission level for theme "theme_two"
    When "Bob" updates theme "theme_two" with name "theme_new"
    Then "theme_two" is successfully updated

  Scenario: A workspace user with no permission level should not be able to update theme
    When "Alice" creates theme "theme_one" for workspace "one"
    Then "theme_one" is successfully created
    When "Bob" updates theme "theme_one"
    Then the theme is not updated due to missing permission level

  Scenario: A workspace user with REVIEWER permission level should not be able to update theme
    When "Alice" creates theme "theme_one" for workspace "one"
    Then "theme_one" is successfully created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Bob" updates theme "theme_one"
    Then the theme is not updated due to missing permission level

  Scenario: A user with OWNER permission level can delete a theme and return remaining theme list
    When "Alice" created theme "theme_one" for workspace "one"
    And "Alice" created theme "theme_two" for workspace "one"
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level for theme "theme_one" to "team"
      | Sales     |
    Then the following "team" have "REVIEWER" permission level over theme "theme_one"
      | Sales     |
    And "Alice" grants "REVIEWER" permission level for theme "theme_two" to "team"
      | Sales     |
    Then the following "team" have "REVIEWER" permission level over theme "theme_two"
      | Sales     |
    When "Alice" creates "DEFAULT" theme variant "Day" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_one" is created successfully
    When "Alice" creates theme variant "Day" for theme "theme_two" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then theme variant "variant_two" is created successfully
    When "Alice" associate theme "theme_one" with icon libraries
      | name        | status   |
      | FONTAWESOME | SELECTED |
      | MICROSOFT   |          |
    Then the association is created successfully
    Then "Alice" list following themes
      | theme_two |
      | theme_one |
    When "Alice" deletes theme "theme_one"
    Then "theme_one" is successfully deleted
    Then "Alice" list following themes
      | theme_two |
    When "Alice" deletes theme "theme_two"
    Then "theme_two" is successfully deleted
    When "Alice" lists the themes
    Then the theme has empty list

  Scenario: A user with OWNER permission level can delete the only theme available and return an empty list
    When "Alice" created theme "theme_one" for workspace "one"
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level for theme "theme_one" to "team"
      | Sales     |
    Then the following "team" have "REVIEWER" permission level over theme "theme_one"
      | Sales     |
    When "Alice" creates "DEFAULT" theme variant "Day" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_one" is created successfully
    When "Alice" deletes theme "theme_one"
    Then "theme_one" is successfully deleted
    When "Alice" lists the themes
    Then the theme has empty list

  Scenario: A user with CONTRIBUTOR permission level should not be able to delete a theme
    When "Alice" created theme "theme_one" for workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" deletes theme "theme_one"
    Then delete fails for "theme_one" due to missing permission level

  Scenario: User should be able to delete theme and its element association
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    Given "Alice" has created activity "UNIT_TWO" inside project "TRO"
    When "Alice" created theme "theme_one" for workspace "one"
    And "Alice" has created a "Sales" team
    And "Alice" grants "REVIEWER" permission level for theme "theme_one" to "team"
      | Sales     |
    Then the following "team" have "REVIEWER" permission level over theme "theme_one"
      | Sales     |
    When "Alice" creates "DEFAULT" theme variant "Day" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_one" is created successfully
    Given "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    Given "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_TWO"
    Then theme "theme_one" is successfully associated with activity "UNIT_TWO"
    When she saves configuration for the "UNIT_ONE" activity
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    Then the activity configuration is successfully saved
    When "Alice" deletes theme "theme_one"
    Then "theme_one" is successfully deleted
    When "Alice" lists the themes
    Then the theme has empty list
    When she fetches the "UNIT_ONE" activity
    Then the theme "theme_two" information is deleted from unit "UNIT_ONE"
