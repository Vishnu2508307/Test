package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.smartsparrow.asset.data.AssetProvider;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.courseware.LearnerAssetHelper;

public class AssetSteps {

    public static final String ASSET_URN = "asset_urn";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} creates an asset with")
    public void createsAnAssetWith(final String accountName, final Map<String, String> args) {
        authenticationSteps.authenticateUser(accountName);


        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.asset.create")
                .addField("url", args.get("url"))
                .addField("mediaType", args.get("mediaType"))
                .addField("assetVisibility", args.get("visibility"))
                .addField("assetProvider", args.get("provider"))
                .build());
    }

    @Then("the asset {string} is created successfully")
    public void theAssetIsCreatedSuccessfully(String assetName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.asset.create.ok")
                .extractFromPayload("$.response.asset.urn", nameFrom(assetName, "urn"))
        );
    }

    @Given("{string} has created asset {string} with")
    public void hasCreatedAssetWith(final String accountName, final String assetName, final Map<String, String> args) {
        createsAnAssetWith(accountName, args);
        theAssetIsCreatedSuccessfully(assetName);
    }

    @When("student {string} fetches the asset {string}")
    public void studentFetchesTheAsset(String accountName, String assetName) {

        authenticationSteps.authenticatesViaIes(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "learner.asset.get")
                .addField("urn", interpolate(nameFrom(assetName, "urn")))
                .build());
    }

    @Then("{string} asset is returned with {string} provider and {string} url")
    public void assetIsReturnedWithProviderAndUrl(String assetName, String assetProvider, String url) {
        if (assetProvider.equals(AssetProvider.EXTERNAL.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath("$.type", "learner.asset.get.ok")
                    .jsonPath("$.response.asset.urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath("$.response.asset.asset.assetProvider", assetProvider)
                    .jsonPath("$.response.asset.source.url", url)
            );
        } else if (assetProvider.equals(AssetProvider.AERO.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath("$.type", "learner.asset.get.ok")
                    .jsonPath("$.response.asset.urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath("$.response.asset.asset.assetProvider", assetProvider)
                    .jsonPath("$.response.asset.source.original", "@notEmpty()@")
                    .jsonPath("$.response.asset.metadata", "@notEmpty()@")
                    .validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                        @SuppressWarnings("rawtypes")
                        @Override
                        public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                            Map response = (Map) payload.get("response");
                            Map asset = (Map) response.get("asset");
                            Map source = (Map) asset.get("source");
                            Map original = (Map) source.get("original");

                            assertTrue(original.get("url").toString().contains(url));
                        }
                    })
            );
        } else {
            fail(String.format("unhandled assetProvider %s", assetProvider));
        }
    }

    @Then("{string} can fetch asset {string} from the workspace with")
    public void canFetchAssetFromTheWorkspaceWith(final String accountName, final String assetName, final Map<String, String> args) {

        final String assetProvider = args.get("assetProvider");
        final String url = args.get("url");

        authenticationSteps.authenticateUser(accountName);
        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "author.asset.get")
                .addField("urn", interpolate(nameFrom(assetName, "urn")));

        messageOperations.sendJSON(runner, payload.build());

        if (assetProvider.equals(AssetProvider.EXTERNAL.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath("$.type", "author.asset.get.ok")
                    .jsonPath("$.response.asset.urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath("$.response.asset.asset.assetProvider", assetProvider)
                    .jsonPath("$.response.asset.source.url", url)
            );
        } else if (assetProvider.equals(AssetProvider.AERO.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath("$.type", "author.asset.get.ok")
                    .jsonPath("$.response.asset.urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath("$.response.asset.asset.assetProvider", assetProvider)
                    .jsonPath("$.response.asset.source.original", "@notEmpty()@")
                    .jsonPath("$.response.asset.metadata", "@notEmpty()@")
                    .validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                        @SuppressWarnings("rawtypes")
                        @Override
                        public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                            Map response = (Map) payload.get("response");
                            Map asset = (Map) response.get("asset");
                            Map source = (Map) asset.get("source");
                            Map original = (Map) source.get("original");

                            assertTrue(original.get("url").toString().contains(url));
                        }
                    })
            );
        } else {
            fail(String.format("unhandled assetProvider %s", assetProvider));
        }
    }

    @Then("{string} activity has {string} asset with")
    public void activityHasAssetWith(final String activityName, final String assetName, final Map<String, String> args) {
        final String assetProvider = args.get("assetProvider");
        final String url = args.get("url");

        if (assetProvider.equals(AssetProvider.EXTERNAL.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath("$.type", "author.activity.get.ok")
                    .jsonPath("$.response.activity.assets", "@notEmpty()@")
                    .jsonPath("$.response.activity.assets[0].urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath("$.response.activity.assets[0].asset.assetProvider", assetProvider)
                    .jsonPath("$.response.activity.assets[0].source.url", url)
            );
        } else if (assetProvider.equals(AssetProvider.AERO.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath("$.type", "author.activity.get.ok")
                    .jsonPath("$.response.activity.assets", "@notEmpty()@")
                    .jsonPath("$.response.activity.assets[0].urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath("$.response.activity.assets[0].asset.assetProvider", assetProvider)
                    .validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                        @SuppressWarnings("rawtypes")
                        @Override
                        public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                            Map response = (Map) payload.get("response");
                            Map activity = (Map) response.get("activity");
                            List assets = (List) activity.get("assets");
                            Map asset = (Map) assets.get(0);
                            Map source = (Map) asset.get("source");
                            Map original = (Map) source.get("original");
                            assertTrue(original.get("url").toString().contains(url));
                        }
                    })
            );
        } else {
            fail(String.format("unhandled assetProvider %s", assetProvider));
        }
    }

    @When("student {string} fetches assets for {string} activity in {string}")
    public void studentFetchesAssetsForActivityIn(final String accountName, final String activityName,
                                                  final String deploymentName) {
        authenticationSteps.authenticatesViaIes(accountName);

        messageOperations.sendGraphQL(runner, LearnerAssetHelper.queryPublishedActivityAssets(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(activityName, "id"))
                ));
    }

    @Then("asset {string} is returned with")
    public void assetIsReturnedWith(final String assetName, final Map<String, String> args) {
        final String assetProvider = args.get("assetProvider");
        final String url = args.get("url");

        final String basePath = "$.response.data.learn.cohort.deployment[0].activity.assets.edges[0].node";

        if (assetProvider.equals(AssetProvider.EXTERNAL.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath(basePath + ".urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath(basePath + ".asset.assetProvider", assetProvider)
                    .jsonPath(basePath + ".source.url", url)
            );

        } else if (assetProvider.equals(AssetProvider.AERO.name())) {
            messageOperations.receiveJSON(runner, action -> action
                    .jsonPath(basePath + ".urn", interpolate(nameFrom(assetName, "urn")))
                    .jsonPath(basePath + ".asset.assetProvider", assetProvider)
                    .validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                        @SuppressWarnings("rawtypes")
                        @Override
                        public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                            Map response = (Map) payload.get("response");
                            Map data = (Map) response.get("data");
                            Map learn = (Map) data.get("learn");
                            Map cohort = (Map) learn.get("cohort");
                            List deployments = (List) cohort.get("deployment");
                            Map deploymentOne = (Map) deployments.get(0);
                            Map activity = (Map) deploymentOne.get("activity");
                            Map assets = (Map) activity.get("assets");
                            List edges = (List) assets.get("edges");
                            Map edge = (Map) edges.get(0);
                            Map node = (Map) edge.get("node");
                            Map source = (Map) node.get("source");
                            Map original = (Map) source.get("original");
                            assertTrue(original.get("url").toString().contains(url));
                        }
                    })
            );
        } else {
            fail(String.format("unhandled assetProvider %s", assetProvider));
        }
    }

    @When("{string} fetches icon assets for following icon libraries")
    public void fetchesIconAssetsForFollowingIconLibraries(String user, List<String> iconLibraries) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.icon.asset.list")
                .addField("iconLibraries", iconLibraries)
                .build();
        messageOperations.sendJSON(runner, message);
    }

    @Then("the icon assets are successfully fetched")
    public void theIconAssetsAreSuccessfullyFetched() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAssetsByIconLibrary,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualAssetsByIconLibrary);

                        for (int i = 0; i < actualAssetsByIconLibrary.size(); i++) {
                            final Map assetsByIconLibrary = (Map) actualAssetsByIconLibrary.get(i);
                            assertNotNull(assetsByIconLibrary.get("iconLibrary"));
                            assertNotNull(assetsByIconLibrary.get("assetUrn"));
                            assertNotNull(assetsByIconLibrary.get("metadata"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "iconAssetSummaries";
                    }

                    @Override
                    public String getType() {
                        return "author.icon.asset.list.ok";
                    }
                }));
    }

    @Then("icon asset has empty list")
    public void iconAssetHasEmptyList() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAssetsByIconLibrary,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualAssetsByIconLibrary);
                        assertTrue(actualAssetsByIconLibrary.isEmpty());
                    }

                    @Override
                    public String getRootElementName() {
                        return "iconAssetSummaries";
                    }

                    @Override
                    public String getType() {
                        return "author.icon.asset.list.ok";
                    }
                }));
    }

    @When("{string} fetches asset details with limit {string} for following assets")
    public void fetchesAssetDetailsWithLimitForFollowingAssets(String user, String limit, List<String> assetUrns) {
        authenticationSteps.authenticateUser(user);

        List<String> ids = assetUrns.stream()
                .map(assetName -> interpolate(nameFrom(assetName, "urn")))
                .collect(Collectors.toList());

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.asset.list")
                .addField("assetUrns", ids)
                .addField("limit", Integer.parseInt(limit))
                .build());
    }

    @Then("{string} fetched asset details successfully")
    public void fetchedAssetDetailsSuccessfully(String user) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAssetPayloads,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualAssetPayloads);
                        for (int i = 0; i < actualAssetPayloads.size(); i++) {
                            final Map assetPayload = (Map) actualAssetPayloads.get(i);
                            assertNotNull(assetPayload.get("urn"));
                            assertNotNull(assetPayload.get("asset"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "assetPayloads";
                    }

                    @Override
                    public String getType() {
                        return "author.asset.list.ok";
                    }
                }));
    }

    @Then("fetching asset details fails with message {string} and code {int}")
    public void fetchingAssetDetailsFailsWithMessageAndCode(String errorMessage, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                                                                               "    \"type\": \"author.asset.list.error\"," +
                                                                               "    \"code\": " + code + "," +
                                                                               "    \"message\": \"" + errorMessage + "\"," +
                                                                               "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @And("{string} has created asset {string} with metadata")
    public void hasCreatedAssetWithMetadata(String accountName,String assetName, Map<String, String> metadata) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.asset.create")
                .addField("url", "https://url.tdl")
                .addField("mediaType", "image")
                .addField("assetVisibility", "GLOBAL")
                .addField("assetProvider", "EXTERNAL")
                .addField("metadata", metadata)
                .build());
        theAssetIsCreatedSuccessfully(assetName);
    }

    @When("{string} updated metadata for an asset {string} with")
    public void updatedMetadataForAnAssetWith(String accountName, String assetName, Map<String, String> metadata) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.asset.metadata.update")
                .addField("assetUrn", interpolate(nameFrom(assetName, "urn")))
                .addField("key", metadata.get("key"))
                .addField("value", metadata.get("value"))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.asset.metadata.update.ok\"," +
                                       "\"response\":{" +
                                       "\"assetMetadata\":{" +
                                       "\"assetId\":\"@notEmpty()@\"," +
                                       "\"key\":\"@notEmpty()@\"," +
                                       "\"value\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
                        .extractFromPayload("$.response.assetMetadata.assetId", nameFrom(assetName, "urn")));
    }

    @Then("{string} deletes icon assets for library {string}")
    public void deletesIconAssetsForLibrary(String accountName, String libraryName) {
        authenticationSteps.authenticateUser(accountName);

        String message = new PayloadBuilder()
                .addField("type", "author.icon.asset.delete")
                .addField("iconLibrary", libraryName)
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("icon assets deleted successfully")
    public void iconAssetsDeletedSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.icon.asset.delete.ok\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
