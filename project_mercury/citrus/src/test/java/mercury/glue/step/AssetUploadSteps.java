package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.AssetSteps.ASSET_URN;
import static mercury.glue.step.ProvisionSteps.getSubscriptionIdVar;
import static mercury.glue.wiring.CitrusConfiguration.REST_CLIENT;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.builder.HttpClientRequestActionBuilder;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpClient;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class AssetUploadSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(REST_CLIENT)
    private HttpClient client;

    @SuppressWarnings("Duplicates")
    @When("^\"([^\"]*)\" uploads asset \"([^\"]*)\" with visibility \"([^\"]*)\"$")
    public void uploadsAssetWithVisibility(String accountName, String fileName, String visibility) {
        uploadAssetWithMetadata(accountName, fileName, visibility, null);
    }

    private void uploadAssetWithMetadata(final String accountName, final String fileName, final String visibility,
                                         final Map<String, String> metadata) {
        String bearerToken = null;
        String subscriptionId = null;

        try {
            bearerToken = runner.variable(accountName + "_bearerToken", interpolate(accountName + "_bearerToken"));
            subscriptionId = runner.variable(getSubscriptionIdVar(accountName), interpolate(getSubscriptionIdVar(accountName)));
        } catch (CitrusRuntimeException ex) {
            if (!ex.getMessage().contains("Unknown variable")) {
                throw ex;
            }
        }

        MultiValueMap<String, Object> map = buildFormDataParams(visibility, subscriptionId, metadata);

        setFileParam(fileName, map);

        uploadAsset(map, bearerToken);
    }

    public void theAssetIsUploadedSuccessfully() {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .statusCode(HttpStatus.OK.value())
                .extractFromPayload("$.urn", ASSET_URN));
    }

    @Then("^the asset upload fails with error status (\\d+)$")
    public void theAssetUploadFailsWithErrorStatus(int code) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .status(HttpStatus.valueOf(code)));
    }

    @When("^\"([^\"]*)\" has uploaded asset \"([^\"]*)\" with visibility \"([^\"]*)\"$")
    public void hasUploadedAssetWithVisibility(String accountName, String fileName, String visibility) {
        uploadsAssetWithVisibility(accountName, fileName, visibility);
        theAssetIsUploadedSuccessfully();
    }

    @Then("^\"([^\"]*)\" gets an asset object with$")
    public void getsAnAssetObjectWith(String user, Map<String, String> data) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .jsonPath("$.asset.assetMediaType", data.get("type"))
                .payload("{\n" +
                        "    \"urn\": \"@notEmpty()@\",\n" +
                        "    \"asset\": \"@notEmpty()@\",\n" +
                        "    \"source\": {\n" +
                        "        \"original\": {\n" +
                        "            \"width\": \"@notEmpty()@\",\n" +
                        "            \"url\": \"@notEmpty()@\",\n" +
                        "            \"height\": \"@notEmpty()@\"\n" +
                        "        }\n" +
                        "    },\n" +
                        "    \"metadata\":{}\n" +
                        "}"));
    }


    @Then("{string} gets an asset audio object with")
    public void getsAnAssetAudioObjectWith(String user, Map<String, String> data) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .jsonPath("$.asset.assetMediaType", data.get("type"))
                .payload("{\n" +
                        "    \"urn\": \"@notEmpty()@\",\n" +
                        "    \"asset\": \"@notEmpty()@\",\n" +
                        "    \"source\": {\n" +
                        "        \"original\": {\n" +
                        "            \"url\": \"@notEmpty()@\"\n" +
                        "        }\n" +
                        "    },\n" +
                        "    \"metadata\":{}\n" +
                        "}"));
    }


    @Then("^\"([^\"]*)\" gets an asset object with metadata$")
    public void getsAnAssetObjectWithMetadata(String user, Map<String, String> data) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .jsonPath("$.asset.assetMediaType", data.get("type"))
                .payload("{\n" +
                        "    \"urn\": \"@notEmpty()@\",\n" +
                        "    \"asset\": \"@notEmpty()@\",\n" +
                        "    \"source\": {\n" +
                        "        \"original\": {\n" +
                        "            \"width\": \"@notEmpty()@\",\n" +
                        "            \"url\": \"@notEmpty()@\",\n" +
                        "            \"height\": \"@notEmpty()@\"\n" +
                        "        }\n" +
                        "    },\n" +
                        "    \"metadata\":{\"description\":\"asset description\",\"config\":\"some config\"}}\n" +
                        "}"));
    }

    @SuppressWarnings("Duplicates")
    @When("^\"([^\"]*)\" uploads asset \"([^\"]*)\" with visibility \"([^\"]*)\" and metadata$")
    public void uploadsAssetWithVisibilityAndMetadata(String user, String fileName, String visibility,
                                                      Map<String, String> metadata) {
        uploadAssetWithMetadata(user, fileName, visibility, metadata);
    }

    private void uploadAsset(MultiValueMap<String, Object> map, final String bearerToken) {
        runner.http(builder -> {
            HttpClientRequestActionBuilder requestBuilder = builder.client(REST_CLIENT)
                    .send().post("/asset/save/")
                    .contentType("multipart/form-data")
                    .payload(map);
            if (bearerToken != null) {
                requestBuilder.header("Authorization", "Bearer " + bearerToken);
            }
        });
    }

    private MultiValueMap<String, Object> buildFormDataParams(String visibility, String subscriptionId, Map<String, String> metadata) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        map.set("visibility", visibility);
        map.set("subscriptionId", subscriptionId);
        map.set("metadata", metadata);

        return map;
    }

    private void setFileParam(String fileName, MultiValueMap<String, Object> map) {
        if (!fileName.isEmpty()) {
            URL filePath = getClass().getClassLoader().getResource("asset/" + fileName);
            if (filePath == null) {
                throw new CitrusRuntimeException(String.format("File '%s' is not found", fileName));
            }
            map.set("file", new PathResource(new File(filePath.getFile()).getAbsolutePath()));
        }
    }

    /**
     * We are using this static variable to keep urn for asset created only once so it can be reused in scenarios.
     * For sequential execution the asset will be uploaded only once for all tests, for parallel execution - one asset per feature file.
     */
    private static String PRE_CREATED_ASSET = null;

    @And("^\"([^\"]*)\" has uploaded special \"([^\"]*)\" asset once$")
    public void hasUploadedSpecialAssetOnce(String account, String assetName) {
        if (PRE_CREATED_ASSET == null) {
            hasUploadedAssetWithVisibility(account, "assetUploadTest.jpg", "GLOBAL");
            PRE_CREATED_ASSET = runner.variable(ASSET_URN, "${" + ASSET_URN + "}");
        }
        runner.createVariable(assetName + "_urn", PRE_CREATED_ASSET);
    }

    @Then("{string} gets an asset document object with")
    public void getsAnAssetDocumentObjectWith(String user, Map<String, String> data) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .payload("{\n" +
                        "  \"urn\": \"@notEmpty()@\",\n" +
                        "  \"asset\": {\n" +
                        "    \"id\": \"@notEmpty()@\",\n" +
                        "    \"assetMediaType\": \"" + data.get("type") + "\",\n" +
                        "    \"ownerId\": \"@notEmpty()@\",\n" +
                        "    \"subscriptionId\": \"@notEmpty()@\",\n" +
                        "    \"hash\": \"@notEmpty()@\",\n" +
                        "    \"assetVisibility\": \"@notEmpty()@\",\n" +
                        "    \"assetProvider\": \"AERO\"\n" +
                        "  },\n" +
                        "  \"source\": {\n" +
                        "    \"url\": \"@notEmpty()@\"\n" +
                        "  },\n" +
                        "  \"metadata\": {}\n" +
                        "}"));
    }

    @And("{string} has uploaded asset {string} as {string} with visibility {string} and metadata")
    public void hasUploadedAssetAsWithVisibilityAndMetadata(final String accountName, final String fileName,
                                                            final String assetName, final String visibility,
                                                            final Map<String, String> metadata) {
        uploadAssetWithMetadata(accountName, fileName, visibility, metadata);
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .statusCode(HttpStatus.OK.value())
                .extractFromPayload("$.urn", nameFrom(assetName, "urn")));
    }

    @Then("{string} gets an asset info successfully with")
    public void getsAnAssetInfoSuccessfullyWith(String user, Map<String, String> data) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .jsonPath("$.asset.assetMediaType", data.get("type"))
                .payload("{\n" +
                                 "    \"urn\": \"@notEmpty()@\",\n" +
                                 "    \"asset\": \"@notEmpty()@\",\n" +
                                 "    \"source\": {\n" +
                                 "        \"original\": {\n" +
                                 "            \"width\": \"@notEmpty()@\",\n" +
                                 "            \"url\": \"@notEmpty()@\",\n" +
                                 "            \"height\": \"@notEmpty()@\"\n" +
                                 "        }\n" +
                                 "    },\n" +
                                 "    \"metadata\": {\n" +
                                 "         \"mediaType\": \"@notEmpty()@\",\n" +
                                 "          \"iconLibrary\": \"@notEmpty()@\"\n" +
                                 "      }\n" +
                                 "}"));

    }
}
