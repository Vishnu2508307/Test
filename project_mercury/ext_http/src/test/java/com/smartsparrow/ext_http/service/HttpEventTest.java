package com.smartsparrow.ext_http.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class HttpEventTest {

    ObjectMapper mapper = new ObjectMapper();

    // a typical request/response.
    @Test
    void canParse() throws Exception {

        // @formatter:off
        String json = "["
                + "   {"
                + "      \"operation\": \"request\","
                + "      \"uri\": \"https://httpbin.org/anything\","
                + "      \"method\": \"GET\","
                + "      \"headers\": {"
                + "         \"User-Agent\": \"SPR-aero/1.0\","
                + "         \"host\": \"httpbin.org\""
                + "      }"
                + "   },"
                + "   {"
                + "      \"operation\": \"response\","
                + "      \"uri\": \"https://httpbin.org/anything\","
                + "      \"headers\": {"
                + "         \"X-list-header\": [\"foo\",\"bar\"],"
                + "         \"access-control-allow-credentials\": \"true\","
                + "         \"access-control-allow-origin\": \"*\","
                + "         \"content-type\": \"application/json\","
                + "         \"date\": \"Tue, 21 May 2019 06:53:20 GMT\","
                + "         \"referrer-policy\": \"no-referrer-when-downgrade\","
                + "         \"server\": \"nginx\","
                + "         \"x-content-type-options\": \"nosniff\","
                + "         \"x-frame-options\": \"DENY\","
                + "         \"x-xss-protection\": \"1; mode=block\","
                + "         \"content-length\": \"273\","
                + "         \"connection\": \"Close\""
                + "      },"
                + "      \"statusCode\": 200,"
                + "      \"body\": \"{  \\\"args\\\": {},   \\\"data\\\": \\\"\\\",   \\\"files\\\": {},   \\\"form\\\": {},   \\\"headers\\\": {    \\\"Host\\\": \\\"httpbin.org\\\",     \\\"User-Agent\\\": \\\"SPR-aero/1.0\\\"  },   \\\"json\\\": null,   \\\"method\\\": \\\"GET\\\",   \\\"origin\\\": \\\"203.219.232.162, 203.219.232.162\\\",   \\\"url\\\": \\\"https://httpbin.org/anything\\\"}\","
                + "      \"time\": {"
                + "         \"timingStart\": 1558421599357,"
                + "         \"timings\": {"
                + "            \"socket\": 21.932101999999986,"
                + "            \"lookup\": 222.973928,"
                + "            \"connect\": 736.447118,"
                + "            \"response\": 1579.4140519999999,"
                + "            \"end\": 1581.0306870000002"
                + "         },"
                + "         \"timingPhases\": {"
                + "            \"wait\": 21.932101999999986,"
                + "            \"dns\": 201.04182600000001,"
                + "            \"tcp\": 513.47319,"
                + "            \"firstByte\": 842.9669339999998,"
                + "            \"download\": 1.616635000000315,"
                + "            \"total\": 1581.0306870000002"
                + "         }"
                + "      }"
                + "   }"
                + "]";
        // @formatter:on

        Object o = mapper.readValue(json, new TypeReference<List<HttpEvent>>() {
        });
        List<HttpEvent> values = (List<HttpEvent>) o;
        assertEquals(values.get(0).getOperation(), HttpEvent.Operation.request);
        assertEquals(values.get(1).getOperation(), HttpEvent.Operation.response);
        assertEquals(values.get(1).getStatusCode().intValue(), 200);
    }

    // a request/redirect/response
    @Test
    void canParseRedirect() throws Exception {

        // @formatter:off
        String json = "["
                + "   {"
                + "      \"operation\": \"request\","
                + "      \"uri\": \"https://httpbin.org/absolute-redirect/1\","
                + "      \"method\": \"GET\","
                + "      \"headers\": {"
                + "         \"User-Agent\": \"SPR-aero/1.0\","
                + "         \"host\": \"httpbin.org\""
                + "      }"
                + "   },"
                + "   {"
                + "      \"operation\": \"redirect\","
                + "      \"statusCode\": 302,"
                + "      \"headers\": {"
                + "         \"access-control-allow-credentials\": \"true\","
                + "         \"access-control-allow-origin\": \"*\","
                + "         \"content-type\": \"text/html; charset=utf-8\","
                + "         \"date\": \"Tue, 21 May 2019 07:03:14 GMT\","
                + "         \"location\": \"http://httpbin.org/get\","
                + "         \"referrer-policy\": \"no-referrer-when-downgrade\","
                + "         \"server\": \"nginx\","
                + "         \"x-content-type-options\": \"nosniff\","
                + "         \"x-frame-options\": \"DENY\","
                + "         \"x-xss-protection\": \"1; mode=block\","
                + "         \"content-length\": \"251\","
                + "         \"connection\": \"Close\""
                + "      },"
                + "      \"uri\": \"http://httpbin.org/get\""
                + "   },"
                + "   {"
                + "      \"operation\": \"request\","
                + "      \"uri\": \"http://httpbin.org/get\","
                + "      \"method\": \"GET\","
                + "      \"headers\": {"
                + "         \"User-Agent\": \"SPR-aero/1.0\","
                + "         \"referer\": \"https://httpbin.org/absolute-redirect/1\","
                + "         \"host\": \"httpbin.org\""
                + "      }"
                + "   },"
                + "   {"
                + "      \"operation\": \"response\","
                + "      \"uri\": \"http://httpbin.org/get\","
                + "      \"headers\": {"
                + "         \"access-control-allow-credentials\": \"true\","
                + "         \"access-control-allow-origin\": \"*\","
                + "         \"content-type\": \"application/json\","
                + "         \"date\": \"Tue, 21 May 2019 07:03:15 GMT\","
                + "         \"referrer-policy\": \"no-referrer-when-downgrade\","
                + "         \"server\": \"nginx\","
                + "         \"x-content-type-options\": \"nosniff\","
                + "         \"x-frame-options\": \"DENY\","
                + "         \"x-xss-protection\": \"1; mode=block\","
                + "         \"content-length\": \"244\","
                + "         \"connection\": \"Close\""
                + "      },"
                + "      \"statusCode\": 200,"
                + "      \"body\": \"{  \\\"args\\\": {},   \\\"headers\\\": {    \\\"Host\\\": \\\"httpbin.org\\\",     \\\"Referer\\\": \\\"https://httpbin.org/absolute-redirect/1\\\",     \\\"User-Agent\\\": \\\"SPR-aero/1.0\\\"  },   \\\"origin\\\": \\\"203.219.232.162, 203.219.232.162\\\",   \\\"url\\\": \\\"https://httpbin.org/get\\\"}\","
                + "      \"time\": {"
                + "         \"timingStart\": 1558422194883,"
                + "         \"timings\": {"
                + "            \"socket\": 1.023077000000285,"
                + "            \"lookup\": 1.7412699999999859,"
                + "            \"connect\": 240.27302400000008,"
                + "            \"response\": 818.2948690000003,"
                + "            \"end\": 818.9331010000001"
                + "         },"
                + "         \"timingPhases\": {"
                + "            \"wait\": 1.023077000000285,"
                + "            \"dns\": 0.718192999999701,"
                + "            \"tcp\": 238.5317540000001,"
                + "            \"firstByte\": 578.0218450000002,"
                + "            \"download\": 0.638231999999789,"
                + "            \"total\": 818.9331010000001"
                + "         }"
                + "      }"
                + "   }"
                + "]";
        // @formatter:on

        Object o = mapper.readValue(json, new TypeReference<List<HttpEvent>>() {
        });
        List<HttpEvent> values = (List<HttpEvent>) o;
        assertEquals(values.get(0).getOperation(), HttpEvent.Operation.request);
        assertEquals(values.get(1).getOperation(), HttpEvent.Operation.redirect);
        assertEquals(values.get(2).getOperation(), HttpEvent.Operation.request);
        assertEquals(values.get(3).getOperation(), HttpEvent.Operation.response);
    }

    // a request/error
    @Test
    void canParseError() throws Exception {
        // @formatter:off
        String json = "["
                + "   {"
                + "      \"operation\": \"request\","
                + "      \"uri\": \"http://invalid-domain.tld/\","
                + "      \"method\": \"GET\","
                + "      \"headers\": {"
                + "         \"User-Agent\": \"SPR-aero/1.0\","
                + "         \"host\": \"invalid-domain.tld\""
                + "      }"
                + "   },"
                + "   {"
                + "      \"operation\": \"error\","
                + "      \"body\": \"Error: getaddrinfo ENOTFOUND invalid-domain.tld invalid-domain.tld:80\""
                + "   }"
                + "]";
        // @formatter:on

        Object o = mapper.readValue(json, new TypeReference<List<HttpEvent>>() {
        });
        List<HttpEvent> values = (List<HttpEvent>) o;
        assertEquals(values.get(0).getOperation(), HttpEvent.Operation.request);
        assertEquals(values.get(1).getOperation(), HttpEvent.Operation.error);
        assertNotNull(values.get(1).getBody());
    }
}