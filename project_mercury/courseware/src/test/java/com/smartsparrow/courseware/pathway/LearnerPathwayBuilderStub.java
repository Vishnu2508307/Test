package com.smartsparrow.courseware.pathway;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Stub out the calls to the pathway builder.
 *
 */
public class LearnerPathwayBuilderStub {

    public static void mock(Provider<LearnerPathwayBuilder> mockProvider) {

        LearnerPathwayBuilder mock = Mockito.mock(LearnerPathwayBuilder.class);

        when(mockProvider.get()).thenReturn(mock);

        //
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> deploymentIdCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> changeIdCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<String> configCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<PreloadPathway> preloadPathwayArgumentCaptor = ArgumentCaptor.forClass(PreloadPathway.class);

        // Answer with a mock pathway when using the builder.
        doAnswer(invocation -> {
            PathwayType type = invocation.getArgument(0);
            return LearnerPathwayMock.mockLearnerPathway(
                    idCaptor.getValue(), //
                    type, //
                    deploymentIdCaptor.getValue(), //
                    changeIdCaptor.getValue(),
                    configCaptor.getValue(),
                    preloadPathwayArgumentCaptor.getValue()
            );
        }).when(mock).build(any(PathwayType.class),
                            idCaptor.capture(),
                            deploymentIdCaptor.capture(),
                            changeIdCaptor.capture(),
                            configCaptor.capture(), preloadPathwayArgumentCaptor.capture());
    }

}
