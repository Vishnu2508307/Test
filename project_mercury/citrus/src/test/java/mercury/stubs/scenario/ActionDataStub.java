package mercury.stubs.scenario;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

public class ActionDataStub {

    public static final ImmutableMap<String, String> ALL = ImmutableMap.copyOf(new HashMap<String, String>() {
        {
            put("actions", actions());
            put("updated_actions", updatedActions());
            put("gradePassBackActions", gradePassBackActions());
        }
    });

    public static final String actions() {
        return  "[{" +
                    "\"action\":\"CHANGE_PROGRESS\"," +
                    "\"resolver\":{" +
                        "\"type\":\"LITERAL\"" +
                    "}," +
                    "\"context\":{" +
                        "\"progressionType\":\"INTERACTIVE_COMPLETE\"" +
                    "}" +
                "}]";
    }

    public static final String gradePassBackActions() {
        return  "[{" +
                "\"action\":\"GRADE_PASSBACK\"," +
                "\"resolver\":{" +
                "\"type\":\"LITERAL\"" +
                "}," +
                "\"context\":{" +
                "\"value\":\"2.0\"" +
                "}" +
                "}]";
    }

    public static final String updatedActions() {
        return "[]";
    }
}
