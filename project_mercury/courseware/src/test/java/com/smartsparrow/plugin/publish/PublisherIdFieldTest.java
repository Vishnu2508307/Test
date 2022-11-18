package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PublisherIdFieldTest {
    @InjectMocks
    private PublisherIdField publisherIdField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        UUID publisherId = publisherIdField.parse(new PluginParserContext()
                .setPublisherId(UUID.randomUUID()));
        assertNotNull(publisherId);
    }
}
