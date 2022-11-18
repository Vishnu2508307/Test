Feature: Create/Remove a math asset for an external source

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client

  Scenario: A user without contributor or higher permissions on the project can not create a math asset
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Bob" creates a math asset for the "COURSE" "activity" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    Then the math asset is not created due to missing permission level

  Scenario: A user without contributor or higher permissions on the project can not remove a math asset
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    When "Alice" creates a math asset for the "COURSE" "activity" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    Then the math asset "MATH_ASSET_ONE" is successfully created
    When "Bob" removes a math asset "MATH_ASSET_ONE" from the "COURSE" "activity"
    Then the math asset is not removed due to missing permission level

  Scenario: A user with contributor or higher permissions on the project can create and remove a math asset
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    When "Bob" creates a math asset for the "COURSE" "activity" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    Then the math asset "MATH_ASSET_ONE" is successfully created
    When "Bob" removes a math asset "MATH_ASSET_ONE" from the "COURSE" "activity"
    Then the math asset "MATH_ASSET_ONE" is successfully removed
