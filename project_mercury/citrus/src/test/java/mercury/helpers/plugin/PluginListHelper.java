package mercury.helpers.plugin;

import static mercury.common.Variables.nameFrom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.smartsparrow.plugin.data.PluginFilter;

import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class PluginListHelper {

    public static String authorPluginListRequest() {
        return new PayloadBuilder().addField("type", "author.plugin.list").build();
    }

    public static String authorPluginListRequestWithPluginType(String pluginType) {
        return new PayloadBuilder()
                .addField("type", "author.plugin.list")
                .addField("pluginType", pluginType)
                .build();
    }

    public static String authorPluginListRequestWithPluginTypeAndFilters(String pluginType, List<PluginFilter> pluginfilter) {
        return new PayloadBuilder()
                .addField("type", "author.plugin.list")
                .addField("pluginType", pluginType)
                .addField("pluginFilters", pluginfilter)
                .build();
    }

    public static String authorPluginListOkResponseEmpty() {
        return "{" +
                "  \"type\": \"author.plugin.list.ok\"," +
                "  \"response\": {" +
                "    \"plugins\": []" +
                "  }," +
                "  \"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    @SuppressWarnings("unchecked")
    public static ValidationCallback authorPluginListOkResponse(Map<String, String> expectedPlugins) {
        return new ResponseMessageValidationCallback<List>(List.class) {

            @Override
            public void validate(List payload, Map<String, Object> headers, TestContext context) {
                List<LinkedHashMap<String, Object>> plugins = (List<LinkedHashMap<String, Object>>) payload;
                Map<String, Object> actualPlugins = plugins.stream().collect(HashMap::new,
                        (map, pluginSummary) -> {
                            map.put((String) pluginSummary.get("name"), pluginSummary.get("latestVersion"));
                            assertNotNull(pluginSummary.get("thumbnail"));
                            assertNotNull(pluginSummary.get("tags"));
                        },
                        HashMap::putAll);

                assertEquals(expectedPlugins, actualPlugins);
            }

            @Override
            public String getRootElementName() {
                return "plugins";
            }

            @Override
            public String getType() {
                return "author.plugin.list.ok";
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static ValidationCallback authorPluginListOkResponseWithEmptyList() {
        return new ResponseMessageValidationCallback<List>(List.class) {

            @Override
            public void validate(List payload, Map<String, Object> headers, TestContext context) {
               assertTrue(payload.isEmpty());
            }

            @Override
            public String getRootElementName() {
                return "plugins";
            }

            @Override
            public String getType() {
                return "author.plugin.list.ok";
            }
        };
    }
}
