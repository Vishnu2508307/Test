Feature: Asset uploading

  @noSocket @ignore
  Scenario: The user without bearer token is not authorized to upload asset
    When "UserWithoutAccount" uploads asset "assetUploadTest.jpg" with visibility "GLOBAL"
    Then the asset upload fails with error status 401

  Scenario: The user is able to upload jpg asset
    Given "Alice" is logged in
    When "Alice" uploads asset "assetUploadTest.jpg" with visibility "GLOBAL"
    Then "Alice" gets an asset object with
      | type       | image  |

  Scenario: The user should be able to upload jpg asset with some metadata information
    Given "Alice" is logged in
    When "Alice" uploads asset "assetUploadTest.jpg" with visibility "GLOBAL" and metadata
      | config      | some config       |
      | description | asset description |
    Then "Alice" gets an asset object with metadata
      | type        | image  |
      | config      | some config       |
      | description | asset description |

  Scenario: The user is able to upload mp3 asset
    Given "Alice" is logged in
    When "Alice" uploads asset "assetUploadTest.mp3" with visibility "GLOBAL"
    Then "Alice" gets an asset audio object with
      | type       | audio  |

  Scenario: The user is not able to upload doc asset
    Given "Alice" is logged in
    When "Alice" uploads asset "assetUploadTest.docx" with visibility "GLOBAL"
    Then the asset upload fails with error status 400

  Scenario: The user gets error if asset is not passed to the asset upload REST call
    Given "Alice" is logged in
    When "Alice" uploads asset "" with visibility "ALL"
    Then the asset upload fails with error status 400

  Scenario: The user is able to upload svg asset
    Given "Alice" is logged in
    When "Alice" uploads asset "assetUploadSvgTest.svg" with visibility "GLOBAL"
    Then "Alice" gets an asset object with
      | type      | image |

  Scenario: The user is able to upload document asset ass
    Given "Alice" is logged in
    When "Alice" uploads asset "sampleASS.ass" with visibility "GLOBAL"
    Then "Alice" gets an asset document object with
      | type      | document |

  Scenario: The user is able to upload document asset ssa
    Given "Alice" is logged in
    When "Alice" uploads asset "sampleSSA.ssa" with visibility "GLOBAL"
    Then "Alice" gets an asset document object with
      | type      | document |

  Scenario: The user is able to upload document asset srt
    Given "Alice" is logged in
    When "Alice" uploads asset "sampleSubRip.srt" with visibility "GLOBAL"
    Then "Alice" gets an asset document object with
      | type      | document |

  Scenario: The user is able to upload document asset vtt
    Given "Alice" is logged in
    When "Alice" uploads asset "sampleVTT.vtt" with visibility "GLOBAL"
    Then "Alice" gets an asset document object with
      | type      | document |

  Scenario: The user is able to upload document asset ttml
    Given "Alice" is logged in
    When "Alice" uploads asset "sampleTTML.ttml" with visibility "GLOBAL"
    Then "Alice" gets an asset document object with
      | type      | document |

  Scenario: The user is able to upload jpg asset with CMYK color space
    Given "Alice" is logged in
    When "Alice" uploads asset "assetUploadJpegTest.jpg" with visibility "GLOBAL"
    Then "Alice" gets an asset object with
      | type       | image  |

  Scenario: The user is able to upload MICROSOFT icon asset
    Given "Alice" is logged in
    When "Alice" uploads asset "iconUpload.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon             |
      | iconLibrary | MICROSOFT ICON |
    Then "Alice" gets an asset info successfully with
      | type | icon |

  Scenario: The user is able to upload FONTAWESOME icon asset
    Given "Alice" is logged in
    When "Alice" uploads asset "iconUpload.svg" with visibility "GLOBAL" and metadata
      | mediaType   | icon             |
      | iconLibrary | FONTAWESOME_ICON |
    Then "Alice" gets an asset info successfully with
      | type | icon |

  Scenario: The user is able to upload svg asset with zero dimensions
    Given "Alice" is logged in
    When "Alice" uploads asset "aIc008.svg" with visibility "GLOBAL"
    Then "Alice" gets an asset object with
      | type      | image |