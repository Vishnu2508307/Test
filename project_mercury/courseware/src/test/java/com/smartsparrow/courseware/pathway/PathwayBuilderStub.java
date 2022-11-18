package com.smartsparrow.courseware.pathway;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.UUID;

import org.mockito.ArgumentCaptor;

/**
 * Stub out the calls to the pathway builder.
 *
 */
public class PathwayBuilderStub {

    public static void mock(PathwayBuilder mock) {
        //
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);

        // Answer with a mock pathway when using the builder.
        doAnswer(invocation -> {
            PathwayType type = invocation.getArgument(0);
            return PathwayMock.mockPathway(idCaptor.getValue(), type);
        }).when(mock).build(any(PathwayType.class), idCaptor.capture(),any(PreloadPathway.class));
    }
}
