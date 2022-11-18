package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.learner.data.LearnerSelectedThemePayload;
import com.smartsparrow.learner.data.LearnerThemeByElement;
import com.smartsparrow.learner.data.LearnerThemeGateway;
import com.smartsparrow.learner.data.LearnerThemeVariant;
import com.smartsparrow.workspace.data.ThemePayload;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LearnerThemeServiceTest {

    @InjectMocks
    private LearnerThemeService learnerThemeService;

    @Mock
    LearnerThemeGateway learnerThemeGateway;
    @Mock
    ThemeService themeService;

    private static final UUID themeId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID variantId = UUID.randomUUID();
    private static final String variantName = "Day";
    private static final String themeName = "theme_one";
    private static final String config = " {\"colo\":\"orange\", \"margin\":\"20\"}";
    ThemePayload themePayload;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        themePayload = new ThemePayload()
                .setId(themeId)
                .setName(themeName)
                .setThemeVariants(Arrays.asList(new ThemeVariant()
                                                        .setThemeId(themeId)
                                                        .setVariantId(variantId)
                                                        .setVariantName(variantName)));

        learnerThemeService = new LearnerThemeService(learnerThemeGateway, themeService);
    }

    @Test
    public void test_saveSelectedThemeByElement() {
        when(learnerThemeGateway.persistThemeElement(any(LearnerThemeByElement.class)))
                .thenReturn(Mono.empty());
        when(learnerThemeGateway.persistDefaultThemeVariant(any(LearnerThemeVariant.class)))
                .thenReturn(Mono.empty());
        when(learnerThemeGateway.persistThemeVariant(any(LearnerThemeVariant.class)))
                .thenReturn(Mono.empty());
        when(themeService.fetchThemeByElementId(elementId)).thenReturn(Mono.just(themePayload));

        learnerThemeService.saveSelectedThemeByElement(elementId);
        verify(themeService).fetchThemeByElementId(elementId);
    }

    @Test
    public void test_fetchSelectedTheme() {
        when(learnerThemeGateway.fetchVariantsByThemeId(themeId))
                .thenReturn(Flux.just(new LearnerThemeVariant()
                                              .setThemeId(themeId)
                                              .setVariantId(variantId)
                                              .setConfig(config)
                                              .setVariantName(
                                                      variantName)
                                              .setState(ThemeState.DEFAULT)));

        when(learnerThemeGateway.fetchThemeByElement(elementId))
                .thenReturn(Mono.just(new LearnerThemeByElement()
                                              .setElementId(elementId)
                                              .setThemeId(themeId)
                                              .setThemeName(themeName)));


        LearnerSelectedThemePayload learnerSelectedThemePayload = learnerThemeService.fetchSelectedTheme(
                elementId).block();

        assertNotNull(learnerSelectedThemePayload);
        assertEquals(learnerSelectedThemePayload.getThemeId(), themeId);
        assertEquals(learnerSelectedThemePayload.getThemeName(), themeName);
        assertEquals(learnerSelectedThemePayload.getElementId(), elementId);

        verify(learnerThemeGateway).fetchVariantsByThemeId(themeId);
        verify(learnerThemeGateway).fetchThemeByElement(elementId);
    }

    @Test
    public void test_fetchThemeVariant() {
        when(learnerThemeGateway.fetchVariantsByThemeIdAndVariantId(themeId,
                                                                    variantId))
                .thenReturn(Mono.just(new LearnerThemeVariant()
                                              .setVariantId(variantId)
                                              .setThemeId(themeId)
                                              .setVariantName(variantName)
                                              .setConfig(config)));

        LearnerThemeVariant learnerThemeVariant = learnerThemeService.fetchThemeVariant(themeId, variantId).block();
        assertNotNull(learnerThemeVariant);
        assertEquals(learnerThemeVariant.getThemeId(), themeId);
        assertEquals(learnerThemeVariant.getVariantId(), variantId);
        assertEquals(learnerThemeVariant.getConfig(), config);

        verify(learnerThemeGateway).fetchVariantsByThemeIdAndVariantId(themeId, variantId);

    }
}
