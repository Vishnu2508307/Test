package com.smartsparrow.asset.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.Json;

class AlfrescoUpdateResponseTest {

    private static final String responseBody = AlfrescoResponseBodyStub.RESPONSE_BODY;

    private static final ObjectMapper om = new ObjectMapper();

    private AlfrescoUpdateResponse alfrescoUpdateResponse;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        om.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        final Map<String, String> bodyMap = Json.toMap(responseBody);

        alfrescoUpdateResponse = om.readValue(bodyMap.get("entry"), AlfrescoUpdateResponse.class);

    }

    @Test
    void getProperty_notFound() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoUpdateResponse.getProperty("foo", String.class));

        assertEquals("foo field missing from properties", f.getMessage());
    }

    @Test
    void getProperty() {
        final String version = alfrescoUpdateResponse.getProperty(AlfrescoUpdateResponse.VERSION_FIELD, String.class);

        assertNotNull(version);
        assertEquals("1.0", version);

        final Double width = Double.valueOf(alfrescoUpdateResponse.getProperty(AlfrescoUpdateResponse.WIDTH_FIELD, Integer.class));

        assertNotNull(width);
        assertEquals(370.0, width);
    }

    @Test
    void getDefaultProperty() {
        String value = alfrescoUpdateResponse.getOrDefaultProperty("foo", String.class, "default");

        assertNotNull(value);
        assertEquals("default", value);
    }

    @Test
    void getMimeType() {
        String mimeType = alfrescoUpdateResponse.getMimeType();

        assertNotNull(mimeType);
        assertEquals("image/jpeg", mimeType);
    }

    @Test
    void getPath() {
        final String path = alfrescoUpdateResponse.getPath().get("name").toString();

        assertNotNull(path);
        assertEquals("/Company Home/Sites/bronte---sf-test/documentLibrary/Book3", path);

    }

    @Test
    void getWorkURN() {
        final String workURN = alfrescoUpdateResponse.getProperty(AlfrescoUpdateResponse.WORK_URN_FIELD, String.class);

        assertNotNull(workURN);
        assertEquals("urn:pearson:work:9413bec8-53b7-11ec-bf63-0242ac130002", workURN);

    }
}