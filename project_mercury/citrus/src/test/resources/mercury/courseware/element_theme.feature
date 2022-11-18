Feature: Create and delete element theme association

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And a workspace account "Bob" is created
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" created theme "theme_one" for workspace "one"
    And "Alice" created theme "theme_two" for workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    And "Alice" creates theme variant "Dark" for theme "theme_two" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then theme variant "variant_one_theme_two" is created successfully

  Scenario: A user with contributor or higher permission level over the element is able to create theme association
    Given "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    When "Alice" associate theme "theme_two" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_two" is successfully associated with activity "UNIT_ONE"

  Scenario: A user with Reviewer permission level over the element is not able to create theme association
    Given "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then the association is unsuccessful due to missing permission level

  Scenario: A user with no permission level over the element is not able to create theme association
    When "Bob" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then the association is unsuccessful due to missing permission level

  Scenario: A user with Contributor or higher permission level over the element is able to create theme association
    When "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    When "Bob" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"

  Scenario: User should be able to delete activity and theme association
    Given "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    When she saves configuration for the "UNIT_ONE" activity
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    Then the activity configuration is successfully saved
    When "Alice" delete theme association for "ACTIVITY" "UNIT_ONE"
    Then the theme is successfully deleted

  Scenario: User without contributor permission should not be able to delete activity and theme association
    Given "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    When she saves configuration for the "UNIT_ONE" activity
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    Then the activity configuration is successfully saved
    When "Bob" delete theme association for "ACTIVITY" "UNIT_ONE"
    Then the theme deletion fails due to "401" code with message "Unauthorized: Unauthorized permission level"

  Scenario: User should be able to delete theme association from an activity payload
    Given "Alice" associate theme "theme_two" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_two" is successfully associated with activity "UNIT_ONE"
    When she saves configuration for the "UNIT_ONE" activity
    """
    {"title":"Demo Course", "desc":"Some description"}
    """
    Then the activity configuration is successfully saved
    And "Alice" deletes theme association for "ACTIVITY" "UNIT_ONE"
    When she fetches the "UNIT_ONE" activity
    Then the theme "theme_two" information is deleted from unit "UNIT_ONE"

  Scenario: A user is able to create theme association with root element and send to interactive level in export
    And "Alice" has created a pathway for the "UNIT_ONE" activity
    When "Alice" creates an interactive for this pathway
    Then the interactive is successfully created
    Given "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
