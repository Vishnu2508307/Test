Feature: Asset fetching

  Background:
    Given a workspace account "Alice" is created
    And an ies account "Bob" is provisioned
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: It should allow to fetch an EXTERNAL asset with any possible fetch mean
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created asset "IMAGE_ONE" with
      | visibility | GLOBAL      |
      | provider   | EXTERNAL    |
      | mediaType  | image       |
      | url        | https://url.tdl |
    When "Alice" has added "IMAGE_ONE" asset to "UNIT_ONE" activity
    # fetching asset from the workspace via rtm
    Then "Alice" can fetch asset "IMAGE_ONE" from the workspace with
      | assetProvider | EXTERNAL        |
      | url           | https://url.tdl |
    # fetching asset via workspace activity get
    When "Alice" fetches the "UNIT_ONE" activity
    Then "UNIT_ONE" activity has "IMAGE_ONE" asset with
      | assetProvider | EXTERNAL        |
      | url           | https://url.tdl |
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    # fetching the asset via learner asset get
    When student "Bob" fetches the asset "IMAGE_ONE"
    Then "IMAGE_ONE" asset is returned with "EXTERNAL" provider and "https://url.tdl" url
    # fetching via graphql
    When student "Bob" fetches assets for "UNIT_ONE" activity in "DEPLOYMENT_ONE"
    Then asset "IMAGE_ONE" is returned with
      | assetProvider | EXTERNAL        |
      | url           | https://url.tdl |

  Scenario: It should allow a student to fetch AERO asset details by urn for a published activity
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has uploaded asset "assetUploadTest.jpg" as "IMAGE_ONE" with visibility "GLOBAL" and metadata
      | config      | some config       |
      | description | asset description |
    When "Alice" has added "IMAGE_ONE" asset to "UNIT_ONE" activity
    # fetching asset from the workspace via rtm
    Then "Alice" can fetch asset "IMAGE_ONE" from the workspace with
      | assetProvider | AERO                                           |
      | url           | /original/a4cfbfa41fc225965fda6853158e9da3.jpg |
    # fetching asset via workspace activity get
    When "Alice" fetches the "UNIT_ONE" activity
    Then "UNIT_ONE" activity has "IMAGE_ONE" asset with
      | assetProvider | AERO                                           |
      | url           | /original/a4cfbfa41fc225965fda6853158e9da3.jpg |
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"
    # fetching the asset via learner asset get
    When student "Bob" fetches the asset "IMAGE_ONE"
    Then "IMAGE_ONE" asset is returned with "AERO" provider and "/original/a4cfbfa41fc225965fda6853158e9da3.jpg" url
    When student "Bob" fetches assets for "UNIT_ONE" activity in "DEPLOYMENT_ONE"
    Then asset "IMAGE_ONE" is returned with
      | assetProvider | AERO                                           |
      | url           | /original/a4cfbfa41fc225965fda6853158e9da3.jpg |
    # fetching via graphql
    When student "Bob" fetches assets for "UNIT_ONE" activity in "DEPLOYMENT_ONE"
    Then asset "IMAGE_ONE" is returned with
      | assetProvider | AERO                                           |
      | url           | /original/a4cfbfa41fc225965fda6853158e9da3.jpg |
