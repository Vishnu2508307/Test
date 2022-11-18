package mercury.stubs.scenario;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

public class ConditionDataStub {

    public static final ImmutableMap<String, String> ALL = ImmutableMap.copyOf(new HashMap<String, String>() {
        {
            put("condition", condition());
            put("updated_condition", updatedCondition());
        }
    });

    public static final String condition() {
        return "{" +
                    "\"type\": " +
                    "\"CHAINED_CONDITION\"," +
                    "\"operator\": \"OR\"," +
                    "\"conditions\": [" +
                        "{" +
                            "\"type\": \"CHAINED_CONDITION\"," +
                            "\"operator\": \"AND\"," +
                            "\"conditions\": [" +
                                "{" +
                                    "\"type\": \"EVALUATOR\"," +
                                    "\"operator\": \"IS\"," +
                                    "\"operandType\": \"STRING\"," +
                                    "\"lhs\": {" +
                                        "\"resolver\": {" +
                                            "\"type\": \"LITERAL\"" +
                                        "}," +
                                    "\"value\": \"selection A\"" +
                                    "}," +
                                    "\"rhs\": {" +
                                        "\"resolver\": {" +
                                            "\"type\": \"LITERAL\"" +
                                        "}," +
                                        "\"value\": \"selection A\"" +
                                    "}," +
                                    "\"options\": [{\"IGNORE_CASE\": true},{\"DECIMAL\": 2}]" +
                                "}" +
                            "]" +
                        "}" +
                    "]" +
                "}";
    }

    public static final String updatedCondition() {
        return "{" +
                    "\"type\": " +
                    "\"CHAINED_CONDITION\"," +
                    "\"operator\": \"AND\"," +
                    "\"conditions\": [" +
                        "{" +
                            "\"type\": \"CHAINED_CONDITION\"," +
                            "\"operator\": \"AND\"," +
                            "\"conditions\": [" +
                                "{" +
                                    "\"type\": \"EVALUATOR\"," +
                                    "\"operator\": \"IS\"," +
                                    "\"operandType\": \"STRING\"," +
                                    "\"lhs\": {" +
                                        "\"resolver\": {" +
                                            "\"type\": \"LITERAL\"" +
                                        "}," +
                                    "\"value\": \"selection A\"" +
                                    "}," +
                                    "\"rhs\": {" +
                                        "\"resolver\": {" +
                                            "\"type\": \"LITERAL\"" +
                                        "}," +
                                        "\"value\": \"selection A\"" +
                                    "}," +
                                    "\"options\": [{\"IGNORE_CASE\": true},{\"DECIMAL\": 2}]" +
                                "}" +
                            "]" +
                        "}" +
                    "]" +
                "}";
    }

    public static final String conditionWith(String operator, String operandType, String sourceId, String studentScopeURN,
                                             String context, String literalValue) {
        return "{" +
                "  \"type\": \"CHAINED_CONDITION\"," +
                "  \"operator\": \"OR\"," +
                "  \"conditions\": [" +
                "    {" +
                "      \"type\": \"CHAINED_CONDITION\"," +
                "      \"operator\": \"AND\"," +
                "      \"conditions\": [" +
                "        {" +
                "          \"type\": \"EVALUATOR\"," +
                "          \"operator\": \"" + operator + "\"," +
                "          \"operandType\": \"" + operandType + "\", " +
                "          \"lhs\": {" +
                "            \"resolver\": {" +
                "              \"type\": \"SCOPE\"," +
                "              \"sourceId\": \"" + sourceId + "\"," +
                "              \"studentScopeURN\": \"" + studentScopeURN + "\"," +
                "              \"context\": [\"" + context + "\"]," +
                "              \"schemaProperty\": {" +
                "              \t\"type\": \"list\"," +
                "                \"listType\": \"text\"," +
                "                \"label\": \"My List\"" +
                "              }," +
                "              \"category\": \"responses\"" +
                "            }" +
                "          }," +
                "          \"rhs\": {" +
                "            \"resolver\": {" +
                "              \"type\": \"LITERAL\"" +
                "            }," +
                "            \"value\": \"" + literalValue + "\"" +
                "          }," +
                "          \"options\": [" +
                "            {\"IGNORE_CASE\": true}," +
                "            {\"DECIMAL\": 2}" +
                "          ]" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";
    }

}
