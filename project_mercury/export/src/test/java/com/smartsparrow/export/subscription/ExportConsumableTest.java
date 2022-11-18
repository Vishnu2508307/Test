package com.smartsparrow.export.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ExportProgress;
import com.smartsparrow.util.UUIDs;

public class ExportConsumableTest {

    @Mock
    private ExportProgress exportProgress;

    private static final UUID exportId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void coursewareExport() {
        ExportBroadcastMessage message = new ExportBroadcastMessage()
                .setExportId(exportId)
                .setExportProgress(exportProgress);
        ExportConsumable consumable = new ExportConsumable(message);

        assertEquals(new ExportRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("author.activity.export/%s", exportId),
                     consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}

