package com.smartsparrow.ext_http.service;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ResultNotificationTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void generatedJson() throws IOException {

        String json = "{\n" +
                "  \"state\": {\n" +
                "    \"notificationId\": \"18555920-e86c-11ea-ab75-9def828ff4d5\",\n" +
                "    \"purpose\": \"GENERAL\",\n" +
                "    \"referenceId\": null\n" +
                "  },\n" +
                "  \"result\": [\n" +
                "    {\n" +
                "      \"operation\": \"request\",\n" +
                "      \"uri\": \"http://httpbin.org/post\",\n" +
                "      \"method\": \"POST\",\n" +
                "      \"headers\": {\n" +
                "        \"application-id\": \"bronte\",\n" +
                "        \"x-authorization\": \"eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjVlYjViYzhmNWQyZjViMDEzMTJhYTY1ZSIsInR5cGUiOiJhdCIsImV4cCI6MTU5ODU0Njk1NSwiaWF0IjoxNTk4NTM2MTU0LCJzZXNzaWQiOiJhODdiYzI0MC0wYjQ1LTQ4ZjctOTllMS1kYzgxMTRmZWUwNjcifQ.LI-YlPmd6k80cl4lL1C7sSRBPcAGL-J6OMrr9eax0L7YieoXgwPGgT8878uuYXVpn6knhkf4URqYn6MuYyEpNdXWF6EDIVzDN7Qibs3tKfqCEDzD9WcSu5yJHeIUQ7RHrgTPqe5a1ptFQ_IZyAkHn0pW1u-iGF2B9LOVraIMzBQr9eJUkY4tr017ApAxK2QNzant43yb0TpDIvOLFnDBf0WEq182f5mLYO9YPPyLGyZkBaILnTv3SQzJxYcLYNn2v2LH25-4DnW5LhqhiYQjIag1sWNOhWuFP15Qp5JT8wk2Scw0Vrf78xni6B2oNkOvRNvqH5XL9FsEfROZ00hhtQ\",\n" +
                "        \"User-Agent\": \"SPR-aero/1.0\",\n" +
                "        \"host\": \"httpbin.org\",\n" +
                "        \"accept\": \"application/json\",\n" +
                "        \"content-type\": \"application/json\",\n" +
                "        \"content-length\": 485\n" +
                "      },\n" +
                "      \"body\": \"{\\\"deploymentId\\\":\\\"1761f870-e86c-11ea-ab75-9def828ff4d5\\\",\\\"changeId\\\":\\\"1761f871-e86c-11ea-ab75-9def828ff4d5\\\",\\\"pearsonId\\\":\\\"pearsonId\\\",\\\"cohortId\\\":\\\"1761f872-e86c-11ea-ab75-9def828ff4d5\\\",\\\"elementId\\\":\\\"1761f873-e86c-11ea-ab75-9def828ff4d5\\\",\\\"elementType\\\":\\\"ACTIVITY\\\",\\\"elementPath\\\":[\\\"uuidParent1\\\",\\\"uuidParent2\\\"],\\\"contentType\\\":\\\"type\\\",\\\"summary\\\":\\\"summary\\\",\\\"body\\\":\\\"body\\\",\\\"source\\\":\\\"source\\\",\\\"preview\\\":\\\"preview\\\",\\\"tag\\\":\\\"tag\\\",\\\"id\\\":\\\"1761f870-e86c-11ea-ab75-9def828ff4d5:1761f873-e86c-11ea-ab75-9def828ff4d5\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        mapper.readValue(json, ResultNotification.class);
    }

    @Test
    void testHttpResult_jsonBody() throws IOException {
        String json = "{\n" +
                "      \"operation\": \"response\",\n" +
                "      \"uri\": \"http://httpbin.org/post\",\n" +
                "      \"headers\": {\n" +
                "        \"date\": \"Thu, 27 Aug 2020 13:49:16 GMT\",\n" +
                "        \"content-type\": \"application/json\",\n" +
                "        \"content-length\": \"2184\",\n" +
                "        \"connection\": \"close\",\n" +
                "        \"server\": \"gunicorn/19.9.0\",\n" +
                "        \"access-control-allow-origin\": \"*\",\n" +
                "        \"access-control-allow-credentials\": \"true\"\n" +
                "      },\n" +
                "      \"statusCode\": 200,\n" +
                "      \"body\": {\n" +
                "        \"args\": {},\n" +
                "        \"data\": \"{\\\"deploymentId\\\":\\\"1761f870-e86c-11ea-ab75-9def828ff4d5\\\",\\\"changeId\\\":\\\"1761f871-e86c-11ea-ab75-9def828ff4d5\\\",\\\"pearsonId\\\":\\\"pearsonId\\\",\\\"cohortId\\\":\\\"1761f872-e86c-11ea-ab75-9def828ff4d5\\\",\\\"elementId\\\":\\\"1761f873-e86c-11ea-ab75-9def828ff4d5\\\",\\\"elementType\\\":\\\"ACTIVITY\\\",\\\"elementPath\\\":[\\\"uuidParent1\\\",\\\"uuidParent2\\\"],\\\"contentType\\\":\\\"type\\\",\\\"summary\\\":\\\"summary\\\",\\\"body\\\":\\\"body\\\",\\\"source\\\":\\\"source\\\",\\\"preview\\\":\\\"preview\\\",\\\"tag\\\":\\\"tag\\\",\\\"id\\\":\\\"1761f870-e86c-11ea-ab75-9def828ff4d5:1761f873-e86c-11ea-ab75-9def828ff4d5\\\"}\",\n" +
                "        \"files\": {},\n" +
                "        \"form\": {},\n" +
                "        \"headers\": {\n" +
                "          \"Accept\": \"application/json\",\n" +
                "          \"Application-Id\": \"bronte\",\n" +
                "          \"Content-Length\": \"485\",\n" +
                "          \"Content-Type\": \"application/json\",\n" +
                "          \"Host\": \"httpbin.org\",\n" +
                "          \"User-Agent\": \"SPR-aero/1.0\",\n" +
                "          \"X-Amzn-Trace-Id\": \"Root=1-5f47b9dc-0075c53a855c964bf992790f\",\n" +
                "          \"X-Authorization\": \"eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjVlYjViYzhmNWQyZjViMDEzMTJhYTY1ZSIsInR5cGUiOiJhdCIsImV4cCI6MTU5ODU0Njk1NSwiaWF0IjoxNTk4NTM2MTU0LCJzZXNzaWQiOiJhODdiYzI0MC0wYjQ1LTQ4ZjctOTllMS1kYzgxMTRmZWUwNjcifQ.LI-YlPmd6k80cl4lL1C7sSRBPcAGL-J6OMrr9eax0L7YieoXgwPGgT8878uuYXVpn6knhkf4URqYn6MuYyEpNdXWF6EDIVzDN7Qibs3tKfqCEDzD9WcSu5yJHeIUQ7RHrgTPqe5a1ptFQ_IZyAkHn0pW1u-iGF2B9LOVraIMzBQr9eJUkY4tr017ApAxK2QNzant43yb0TpDIvOLFnDBf0WEq182f5mLYO9YPPyLGyZkBaILnTv3SQzJxYcLYNn2v2LH25-4DnW5LhqhiYQjIag1sWNOhWuFP15Qp5JT8wk2Scw0Vrf78xni6B2oNkOvRNvqH5XL9FsEfROZ00hhtQ\"\n" +
                "        },\n" +
                "        \"json\": {\n" +
                "          \"body\": \"body\",\n" +
                "          \"changeId\": \"1761f871-e86c-11ea-ab75-9def828ff4d5\",\n" +
                "          \"cohortId\": \"1761f872-e86c-11ea-ab75-9def828ff4d5\",\n" +
                "          \"contentType\": \"type\",\n" +
                "          \"deploymentId\": \"1761f870-e86c-11ea-ab75-9def828ff4d5\",\n" +
                "          \"elementId\": \"1761f873-e86c-11ea-ab75-9def828ff4d5\",\n" +
                "          \"elementPath\": [\n" +
                "            \"uuidParent1\",\n" +
                "            \"uuidParent2\"\n" +
                "          ],\n" +
                "          \"elementType\": \"ACTIVITY\",\n" +
                "          \"id\": \"1761f870-e86c-11ea-ab75-9def828ff4d5:1761f873-e86c-11ea-ab75-9def828ff4d5\",\n" +
                "          \"pearsonId\": \"pearsonId\",\n" +
                "          \"preview\": \"preview\",\n" +
                "          \"source\": \"source\",\n" +
                "          \"summary\": \"summary\",\n" +
                "          \"tag\": \"tag\"\n" +
                "        },\n" +
                "        \"origin\": \"3.104.53.10\",\n" +
                "        \"url\": \"http://httpbin.org/post\"\n" +
                "      },\n" +
                "      \"time\": {\n" +
                "        \"timingStart\": 1598536156150,\n" +
                "        \"timings\": {\n" +
                "          \"socket\": 1.0844139999971958,\n" +
                "          \"lookup\": 99.87067399999069,\n" +
                "          \"connect\": 400.853523999991,\n" +
                "          \"response\": 600.0274470000004,\n" +
                "          \"end\": 600.3465399999986\n" +
                "        },\n" +
                "        \"timingPhases\": {\n" +
                "          \"wait\": 1.0844139999971958,\n" +
                "          \"dns\": 98.78625999999349,\n" +
                "          \"tcp\": 300.9828500000003,\n" +
                "          \"firstByte\": 199.17392300000938,\n" +
                "          \"download\": 0.31909299999824725,\n" +
                "          \"total\": 600.3465399999986\n" +
                "        }\n" +
                "      }\n" +
                "    }\n";
        Assertions.assertDoesNotThrow(() -> mapper.readValue(json, HttpEvent.class));
    }
}