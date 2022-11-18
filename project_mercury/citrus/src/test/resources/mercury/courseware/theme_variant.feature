Feature: Create update delete and list theme variant

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" created theme "theme_one" for workspace "one"

  Scenario: A user with owner permission level over theme should be able to create theme variant and update
    When "Alice" creates "DEFAULT" theme variant "Day" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_one" is created successfully
    Then "Alice" get following theme variant from "variant_one" and theme "theme_one"
      | variantName | Day |
      | theme_name  | theme_one |
    Then "Alice" list following themes
      | theme_one |
    When "Alice" updates variant "variant_one" and theme "theme_one" with variant name "Normal" and config
     """
    {"color":"black", "margin":"20"}
    """
    Then default theme variant "variant_one" updated successfully
    Then "Alice" get following theme variant from "variant_one" and theme "theme_one"
      | variantName | Normal |
      | theme_name  | theme_one |
    Then "Alice" list following themes
      | theme_one |

  Scenario: A user with contributor or higher permission level over theme should be able to create and update theme variant
    And "Alice" created theme "theme_two" for workspace "one"
    And "Alice" created theme "theme_three" for workspace "one"
    When "Alice" granted "Bob" with "CONTRIBUTOR" permission level for theme "theme_one"
    When "Alice" granted "Bob" with "CONTRIBUTOR" permission level for theme "theme_two"
    When "Alice" granted "Bob" with "CONTRIBUTOR" permission level for theme "theme_three"
    When "Bob" creates "DEFAULT" theme variant "Sepia" for theme "theme_one" with config
    """
    {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    When "Bob" creates theme variant "Sepia_ONE" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_one" is created successfully
    When "Bob" creates "DEFAULT" theme variant "Normal" for theme "theme_two" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_two" is created successfully
    When "Bob" creates theme variant "Normal_ONE" for theme "theme_two" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_two" is created successfully
    When "Bob" creates "DEFAULT" theme variant "Day" for theme "theme_three" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_three" is created successfully
    When "Bob" creates theme variant "Day_ONE" for theme "theme_three" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_three" is created successfully
    Then "Bob" list following themes
      | theme_three |
      | theme_two   |
      | theme_one   |
    When "Bob" updates variant "variant_one_theme_three" and theme "theme_three" with variant name "Night" and config
     """
    {"color":"black", "margin":"20"}
    """
    Then default theme variant "variant_one_theme_three" updated successfully
    Then "Alice" get following theme variant from "variant_one_theme_three" and theme "theme_three"
      | variantName | Night |
      | theme_name  | theme_three |
    When "Bob" updates variant "variant_two_theme_two" and theme "theme_two" with variant name "Light" and config
     """
    {"color":"black", "margin":"20"}
    """
    Then theme variant "variant_two_theme_two" updated successfully

  Scenario: A user with REVIEWER permission level should not be able to create theme variant
    When "Alice" granted "Bob" with "REVIEWER" permission level for theme "theme_one"
    When "Bob" creates "DEFAULT" theme variant "Day" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then the theme variant is not created due to missing permission level

  Scenario: A user with REVIEWER permission level should not be able to update theme variant
    And "Alice" created theme "theme_two" for workspace "one"
    When "Alice" creates "DEFAULT" theme variant "Normal" for theme "theme_two" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_two" is created successfully
    When "Alice" granted "Bob" with "REVIEWER" permission level for theme "theme_one"
    When "Bob" updates variant "variant_one_theme_two" and theme "theme_two" with variant name "Night" and config
     """
    {"color":"orange", "margin":"20"}
    """
    Then theme variant is not updated due to missing permission level

  Scenario: A user with missing permission level should not be able to create theme variant
    When "Bob" creates "DEFAULT" theme variant "Day" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then the theme variant is not created due to missing permission level

  Scenario: A user with missing permission level should not be able to update theme variant
    When "Alice" creates theme variant "sepia" for theme "theme_one" with config
    """
    {"color":"black", "margin":"10"}
    """
    Then theme variant "variant_one_theme_one" is created successfully
    When "Bob" updates variant "variant_one_theme_one" and theme "theme_one" with variant name "Night" and config
     """
    {"color":"orange", "margin":"20"}
    """
    Then theme variant is not updated due to missing permission level

      # Delete
  Scenario: A user with contributor or higher permission level should able to delete theme variant
    When "Alice" creates theme variant "Day" for theme "theme_one" with config
   """
   {"color":"orange", "margin":"20"}
   """
    Then theme variant "variant_one_theme_one" is created successfully
    Then "Alice" get following theme variant from "variant_one_theme_one" and theme "theme_one"
      | variantName | Day |
      | theme_name  | theme_one |
    When "Alice" deletes theme variant "variant_one_theme_one" for theme "theme_one"
    Then theme variant deleted successfully
    Then "Alice" get empty theme variant from "variant_one_theme_one" and theme "theme_one"

  Scenario: A default theme variant can be deleted
    And "Alice" created theme "theme_two" for workspace "one"
    When "Alice" creates theme variant "Night" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_one_theme_one" is created successfully
    When "Alice" creates theme variant "Day" for theme "theme_two" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then theme variant "variant_one_theme_two" is created successfully
    When "Alice" creates "DEFAULT" theme variant "Sepia" for theme "theme_two" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_two_theme_two" is created successfully
    When "Alice" creates "DEFAULT" theme variant "Normal" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_two_theme_one" is created successfully
    When "Alice" deletes theme variant "variant_two_theme_one" for theme "theme_one"
    Then theme variant deleted successfully
    When "Alice" deletes theme variant "variant_one_theme_one" for theme "theme_one"
    Then theme variant deleted successfully
    Then "Alice" get empty theme variant from "variant_two_theme_one" and theme "theme_one"
    When "Alice" deletes theme variant "variant_two_theme_two" for theme "theme_two"
    Then theme variant deleted successfully
    When "Alice" deletes theme variant "variant_one_theme_two" for theme "theme_two"
    Then theme variant deleted successfully
    Then "Alice" get empty theme variant from "variant_two_theme_two" and theme "theme_two"

  Scenario: A user with contributor permission level should able to delete theme variant
    When "Alice" creates theme variant "Day" for theme "theme_one" with config
  """
  {"color":"orange", "margin":"20"}
  """
    Then theme variant "variant_one_theme_one" is created successfully
    And "Alice" granted "Bob" with "CONTRIBUTOR" permission level for theme "theme_one"
    When "Bob" deletes theme variant "variant_one_theme_one" for theme "theme_one"
    Then theme variant deleted successfully
    Then "Alice" get empty theme variant from "variant_one_theme_one" and theme "theme_one"

  Scenario: A user with REVIEWER permission level should not be able to delete theme variant
    When "Alice" creates theme variant "Day" for theme "theme_one" with config
  """
  {"color":"orange", "margin":"20"}
  """
    Then theme variant "variant_one_theme_one" is created successfully
    And "Alice" granted "Bob" with "REVIEWER" permission level for theme "theme_one"
    When "Bob" deletes theme variant "variant_one_theme_one" for theme "theme_one"
    Then theme variant not deleted due to missing permission level

  Scenario: A user can list themes which has access to with permission level info
    When "Alice" creates "DEFAULT" theme variant "Day" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_one" is created successfully
    Then "Alice" get following theme variant from "variant_one" and theme "theme_one"
      | variantName | Day |
      | theme_name  | theme_one |
    Then "Alice" list following themes
      | theme_one |
    And "Alice" created theme "theme_two" for workspace "one"
    When "Alice" creates "DEFAULT" theme variant "Normal" for theme "theme_two" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_two" is created successfully
    When "Alice" granted "Charlie" with "CONTRIBUTOR" permission level for theme "theme_one"
    When "Alice" granted "Charlie" with "REVIEWER" permission level for theme "theme_two"
    Then "Charlie" list following themes
      | theme_two |
      | theme_one |

  Scenario: A user can list theme which has 2 default theme variants
    When "Alice" creates "DEFAULT" theme variant "one" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_one" is created successfully
    When "Alice" creates "DEFAULT" theme variant "two" for theme "theme_one" with config
    """
    {"color":"orange", "margin":"20"}
    """
    Then default theme variant "variant_two" is created successfully
    Then "Alice" list following themes
      | theme_one |
