Feature: Fetch icon assets and update asset metadata

  Scenario: The user is able to fetch icon assets by library name
    Given "Alice" is logged in
    When "Alice" uploads asset "iconUpload.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon           |
      | iconLibrary | Microsoft Icon |
    Then "Alice" gets an asset info successfully with
      | type | icon |
    When "Alice" fetches icon assets for following icon libraries
      | Microsoft Icon |
    Then the icon assets are successfully fetched

  Scenario: The user is able to fetch icon assets by list of library name
    Given "Alice" is logged in
    When "Alice" uploads asset "iconUpload.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon           |
      | iconLibrary | Microsoft Icon |
    Then "Alice" gets an asset info successfully with
      | type | icon |
    When "Alice" uploads asset "assetUploadSvgTest.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon        |
      | iconLibrary | Fontawesome |
    Then "Alice" gets an asset info successfully with
      | type | icon |
    When "Alice" uploads asset "assetUploadSvgTest.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon       |
      | iconLibrary | My library |
    Then "Alice" gets an asset info successfully with
      | type | icon |
    When "Alice" fetches icon assets for following icon libraries
      | Microsoft Icon |
      | Fontawesome    |
      | My library     |
    Then the icon assets are successfully fetched

  Scenario: For unavailable icon library an empty list is returned
    Given "Alice" is logged in
    When "Alice" uploads asset "iconUpload.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon           |
      | iconLibrary | Microsoft Icon |
    Then "Alice" gets an asset info successfully with
      | type | icon |
    When "Alice" fetches icon assets for following icon libraries
      | FontAwesome |
    Then icon asset has empty list

  Scenario: Fetch asset details with same limit and asset urn list size
    Given "Alice" is logged in
    And "Alice" has created asset "IMAGE_ONE" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    And "Alice" has created asset "IMAGE_TWO" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    When "Alice" fetches asset details with limit "2" for following assets
      |IMAGE_ONE|
      |IMAGE_TWO|
    Then "Alice" fetched asset details successfully

  Scenario: Fetch asset details with limit size greater than asset urn list size
    Given "Alice" is logged in
    And "Alice" has created asset "IMAGE_ONE" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    And "Alice" has created asset "IMAGE_TWO" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    And "Alice" has created asset "IMAGE_THREE" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    When "Alice" fetches asset details with limit "4" for following assets
      |IMAGE_ONE|
      |IMAGE_TWO|
      |IMAGE_THREE|
    Then "Alice" fetched asset details successfully

  Scenario: Fetch asset details with limit size less than asset urn list size
    Given "Alice" is logged in
    And "Alice" has created asset "IMAGE_ONE" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    And "Alice" has created asset "IMAGE_TWO" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    And "Alice" has created asset "IMAGE_THREE" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    When "Alice" fetches asset details with limit "2" for following assets
      |IMAGE_ONE|
      |IMAGE_TWO|
      |IMAGE_THREE|
    Then fetching asset details fails with message "asset urn list exceeds the limit size" and code 400

  Scenario: The user is able to update existing metadata key and value for an asset
    Given "Alice" is logged in
    Given "Alice" has created asset "IMAGE_ONE" with metadata
      | altText   | image one             |
      | originalAssetName | IMAGE_ONE.jpg |
    Then "Alice" updated metadata for an asset "IMAGE_ONE" with
      | key   | altText     |
      | value | small image |

  Scenario: The user is able to add new metadata key and value for an asset
    Given "Alice" is logged in
    Given "Alice" has created asset "IMAGE_ONE" with metadata
      | altText   | image one             |
      | originalAssetName | IMAGE_ONE.jpg |
    Then "Alice" updated metadata for an asset "IMAGE_ONE" with
      | key   | description             |
      | value | This is original image  |

  Scenario: The user is able to delete icon assets for Microsoft icon library
    Given "Alice" is logged in
    When "Alice" uploads asset "iconUpload.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon           |
      | iconLibrary | Microsoft Icon |
    When "Alice" uploads asset "assetUploadSvgTest.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon           |
      | iconLibrary | Microsoft Icon |
    When "Alice" uploads asset "assetUploadSvgTest.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon           |
      | iconLibrary | Microsoft Icon |
    When "Alice" fetches icon assets for following icon libraries
      | Microsoft Icon |
      | Microsoft Icon |
      | Microsoft Icon |
    Then the icon assets are successfully fetched
    Then "Alice" deletes icon assets for library "Microsoft Icon"
    Then icon assets deleted successfully
    When "Alice" fetches icon assets for following icon libraries
      | Microsoft Icon |
    Then icon asset has empty list

  Scenario: The user is able to delete icon assets for Font awesome library
    Given "Alice" is logged in
    When "Alice" uploads asset "iconUpload.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon         |
      | iconLibrary | Font awesome |
    When "Alice" uploads asset "assetUploadSvgTest.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon         |
      | iconLibrary | Font awesome |
    When "Alice" uploads asset "assetUploadSvgTest.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon           |
      | iconLibrary | Microsoft Icon |
    When "Alice" fetches icon assets for following icon libraries
      | Font awesome   |
      | Font awesome   |
      | Microsoft Icon |
    Then the icon assets are successfully fetched
    Then "Alice" deletes icon assets for library "Font awesome"
    Then icon assets deleted successfully
    When "Alice" fetches icon assets for following icon libraries
      | Font awesome |
    Then icon asset has empty list
    When "Alice" fetches icon assets for following icon libraries
      | Microsoft Icon |
    Then the icon assets are successfully fetched

