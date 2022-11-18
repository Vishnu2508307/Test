Feature: Subscriptions for courseware export

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"

  Scenario: A workspace user can list exports for that workspace
    Given an account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE" with export type "EPUB_PREVIEW"
    When "Bob" lists all the exports for workspace "one"
    Then the following exports are listed for workspace "one" with export type "EPUB_PREVIEW"
    | UNIT_ONE |

  Scenario: A workspace user can list exports for that workspace with default export type
    Given an account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE"
    When "Bob" lists all the exports for workspace "one"
    Then the following exports are listed for workspace "one" with export type "GENERIC"
      | UNIT_ONE |

  Scenario: A project user can list exports for that workspace
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE" with export type "EPUB_PREVIEW"
    When "Bob" lists all the exports for project "TRO"
    Then the following exports are listed for project "TRO" with export type "EPUB_PREVIEW"
      | UNIT_ONE |

  Scenario: A project user can list exports for that workspace with default export type
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE"
    When "Bob" lists all the exports for project "TRO"
    Then the following exports are listed for project "TRO" with export type "GENERIC"
      | UNIT_ONE |

  Scenario: A workspace user can list exports for that workspace with selected theme payload
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    When "Alice" creates theme variant "Normal_ONE" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_two_theme_one" is created successfully
    When "Alice" creates theme variant "Normal_TWO" for theme "theme_one" with config
     """
     {"color":"orange", "margin":"20"}
     """
    Then theme variant "variant_three_theme_one" is created successfully
    When "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    And an account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE" with export type "EPUB_PREVIEW"
    When "Bob" lists all the exports for workspace "one"
    Then the following exports are listed for workspace "one" with export type "EPUB_PREVIEW"
      | UNIT_ONE |

  Scenario: A workspace user can list exports for that workspace
    Given an account "Bob" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created an export of activity "UNIT_ONE" with export type "EPUB_CP_PUBLISH" and metadata "landmarks"
    When "Bob" lists all the exports for workspace "one"
    Then the following exports are listed for workspace "one" with export type "EPUB_CP_PUBLISH"
      | UNIT_ONE |