Feature: Create an asset for an external source

  Background:
    Given an account "Alice" is created

  Scenario: It should allow a workspace role user to create an asset for an external source
    When "Alice" creates an asset with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    Then the asset "ASSET_ONE" is created successfully