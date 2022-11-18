Feature: Fetch a math asset

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"

  Scenario: It should allow a workspace role user to fetch a math asset
    When "Alice" creates a math asset for the "COURSE" "activity" with
      | mathML  | <math><mn>1</mn><mo>-</mo><mn>2</mn></math> |
      | altText | e equals m c squared                        |
    Then the math asset "MATH_ASSET_ONE" is successfully created
    When "Alice" fetches the math asset "MATH_ASSET_ONE"
    Then the math asset "MATH_ASSET_ONE" is successfully fetched
