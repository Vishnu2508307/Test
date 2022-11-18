package com.smartsparrow.export.subscription;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ExportProgress;
import com.smartsparrow.util.UUIDs;

public class ExportProducerTest {

    @InjectMocks
    private ExportProducer exportProducer;

    @Inject
    private ExportProgress exportProgress;

    private static final UUID exportId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        exportProducer.buildExportConsumable(exportId, exportProgress);
        assertNotNull(exportProducer.getEventConsumable());
    }

}
