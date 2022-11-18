package mercury.helpers.plugin;

import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.stream.Collectors;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.smartsparrow.plugin.data.ManifestView;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;

public class PluginHelper {

    public static String getPluginWorkspaceRequest(String pluginId) {
        return new PayloadBuilder()
                .addField("type", "workspace.plugin.get")
                .addField("pluginId", pluginId)
                .build();
    }

    public static String getPluginAuthorRequest(String pluginId) {
        return new PayloadBuilder()
                .addField("type", "author.plugin.get")
                .addField("pluginId", pluginId)
                .build();
    }

    public static String listPluginVersionRequest(String pluginId) {
        return new PayloadBuilder()
                .addField("type", "workspace.plugin.version.list")
                .addField("pluginId", pluginId)
                .build();
    }

    public static String unpublishPluginVersionRequest(String pluginId, String version) {
        String[] versions = version.split("\\.");
        return new PayloadBuilder()
                .addField("type", "author.plugin.version.unpublish")
                .addField("pluginId", pluginId)
                .addField("major", versions[0])
                .addField("minor", versions[1])
                .addField("patch", versions[2])
                .build();
    }

    public static String deletePluginVersionRequest(String pluginId, String version) {
        return new PayloadBuilder()
                .addField("type", "author.plugin.version.delete")
                .addField("pluginId", pluginId)
                .addField("version", version)
                .build();
    }

    public static String deletePluginVersionRequestOk(String pluginId, String version) {
        return new PayloadBuilder()
                .addField("type", "author.plugin.version.delete.ok")
                .addField("pluginId", pluginId)
                .addField("version", version)
                .build();
    }

    public static String createPluginRequest(String pluginName, String pluginType) {
        return createPluginRequest(pluginName, pluginType, null, null);
    }

    public static String createPluginRequest(String pluginName, String pluginType, UUID pluginId, String publishMode) {
        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "workspace.plugin.create")
                .addField("name", pluginName);
        if (pluginType != null) {
            payload = payload.addField("pluginType", pluginType);
        }
        if (pluginId != null) {
            payload = payload.addField("pluginId", pluginId);
        }
        if (publishMode != null) {
            payload = payload.addField("publishMode", publishMode);
        }

        return payload.build();
    }

    public static String updatePluginRequest(String pluginId, String publishMode) {
        return new PayloadBuilder()
                .addField("type", "workspace.plugin.update")
                .addField("pluginId", pluginId)
                .addField("publishMode",publishMode).build();
    }

    public static String createPluginOkResponseAndExtractId(String expectedPluginName, String pluginIdVar, String type, String mode) {
        return "{\"type\":\"workspace.plugin.create.ok\", " +
                "\"response\":{\"plugin\": {" +
                "      \"pluginId\": \"@variable(" + pluginIdVar + ")@\"," +
                "      \"name\": \"" + expectedPluginName + "\"," +
                (type != null ? "\"type\": \"" + type + "\"," : "") +
                "      \"subscriptionId\": \"@notEmpty()@\"," +
                "      \"createdAt\":\"@notEmpty()@\"," +
                "      \"publishMode\":\"" + (mode != null ? mode : "DEFAULT") + "\"," +
                "      \"creator\": {" +
                "        \"accountId\": \"@notEmpty()@\"," +
                "        \"subscriptionId\": \"@notEmpty()@\"," +
                "        \"iamRegion\": \"@notEmpty()@\"," +
                "        \"primaryEmail\": \"@notEmpty()@\"," +
                "        \"roles\": \"@notEmpty()@\"," +
                "        \"email\": \"@notEmpty()@\"," +
                "        \"authenticationType\": \"@notEmpty()@\"" +
                "      }" +
                "    }" +
                "  }," +
                "\"replyTo\":\"@notEmpty()@\"}";
    }

    @SuppressWarnings("unchecked")
    public static ValidationCallback listPluginVersionResponse(List<String> expectedVersions) {
        return new ResponseMessageValidationCallback<List>(List.class) {
            @Override
            public void validate(List payload, Map<String, Object> headers, TestContext context) {
                List<Map<String, Object>> versions = (List<Map<String, Object>>) payload;
                List<String> actualVersions = versions.stream().map(map -> (String) map.get("version")).collect(Collectors.toList());

                assertTrue(actualVersions.containsAll(expectedVersions));
            }

            @Override
            public String getRootElementName() {
                return "versions";
            }

            @Override
            public String getType() {
                return "workspace.plugin.version.list.ok";
            }
        };

    }

    public static String listPluginCollaboratorsRequest(String pluginId, Integer limit) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.collaborator.summary");
        payload.addField("pluginId", pluginId);
        if (limit != null) {
            payload.addField("limit", limit);
        }
        return payload.build();
    }

    public static ValidationCallback validatePluginCollaboratorList(int expectedTotal, List<CollaboratorItem> expectedItems) {
        return new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
            @Override
            public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {

                assertEquals("workspace.plugin.collaborator.summary.ok", payload.getType());
                assertEquals(expectedTotal, (int) payload.getResponse().get("total"));

                Set<Map> expectedTeams = new HashSet<>();
                Set<Map> expectedAccounts = new HashSet<>();

                for (CollaboratorItem item : expectedItems) {
                    Map<String, String> expected = new HashMap<>();
                    if ("team".equals(item.type)) {
                        expected.put("teamId", context.getVariable(Variables.nameFrom(item.name, "id")));
                        expected.put("permission", item.permissionLevel);
                        expectedTeams.add(expected);
                    } else if ("account".equals(item.type)) {
                        expected.put("accountId", context.getVariable(getAccountIdVar(item.name)));
                        expected.put("permission", item.permissionLevel);
                        expectedAccounts.add(expected);
                    } else {
                        throw new CitrusRuntimeException("collaborator should be team or account");
                    }
                }

                ArrayList collaboratorsTeams = (ArrayList) ((Map) payload.getResponse().get("collaborators")).get("teams");
                ArrayList collaboratorsAccounts = (ArrayList) ((Map) payload.getResponse().get("collaborators")).get("accounts");

                Set<Map> actualTeams = new HashSet<>();
                if (collaboratorsTeams != null) {
                    for (Object coll : collaboratorsTeams) {
                        Map<String, String> actual = new HashMap<>();
                        actual.put("teamId", (String) ((Map) ((Map) coll).get("team")).get("id"));
                        actual.put("permission", (String) ((Map) coll).get("permissionLevel"));
                        actualTeams.add(actual);
                    }
                }
                Set<Map> actualAccounts = new HashSet<>();
                if (collaboratorsAccounts != null) {
                    for (Object coll : collaboratorsAccounts) {
                        Map<String, String> actual = new HashMap<>();
                        actual.put("accountId", (String) ((Map) ((Map) coll).get("account")).get("accountId"));
                        actual.put("permission", (String) ((Map) coll).get("permissionLevel"));
                        actualAccounts.add(actual);
                    }
                }

                assertEquals(expectedTeams, actualTeams);
                assertEquals(expectedAccounts, actualAccounts);
            }
        };
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "The citrus writes to this fields with values from feature files.")
    public static class CollaboratorItem {
        public String name;
        public String permissionLevel;
        public String type;
    }

    public static void verifySummary(PluginSummaryPayload summary, TestContext context, String expectedPluginName,
                                     String expectedPluginIdVar) {
        assertEquals(context.getVariable(expectedPluginIdVar), summary.getPluginId().toString());
        assertEquals(expectedPluginName, summary.getName());
        //FIXME: should not check the type on creation however it should check the type on fetch when published
//        assertEquals(PluginType.COMPONENT, summary.getType());
        assertNotNull(summary.getAccountPayload().getAccountId());
        assertNotNull(summary.getSubscriptionId());
    }

    public static void verifyManifest(PluginManifest manifest, TestContext context, String expectedPluginIdVar,
                                      String expectedVersion, String expectedDescription) {
        assertEquals(context.getVariable(expectedPluginIdVar), manifest.getPluginId().toString());
        assertEquals(expectedVersion, manifest.getVersion());
        assertNotNull(manifest.getZipHash());
        assertNotNull(manifest.getConfigurationSchema());
        assertNotNull(manifest.getPublisherId());
        assertEquals(expectedDescription, manifest.getDescription());
        assertTrue(manifest.getThumbnail().contains("img/thumbnail.png"));
        assertTrue(manifest.getScreenshots().iterator().next().contains("img/mercury.jpg"));
    }

    public static void verifyView(ManifestView view, TestContext context, String expectedPluginIdVar,
                                  String expectedVersion) {
        assertEquals(context.getVariable(expectedPluginIdVar), view.getPluginId().toString());
        assertEquals(expectedVersion, view.getVersion());
        assertEquals("LEARNER", view.getContext());
        assertTrue(view.getEntryPointPath().contains("index.js"));
        assertEquals("some random content", view.getEntryPointData());
        assertTrue(view.getPublicDir() == null || view.getPublicDir().isEmpty());
        assertNull(view.getEditorMode());
        assertEquals("javascript", view.getContentType());
    }
}
