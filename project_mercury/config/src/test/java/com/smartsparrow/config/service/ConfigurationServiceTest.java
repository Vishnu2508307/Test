package com.smartsparrow.config.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.config.data.Configuration;
import com.smartsparrow.config.data.ConfigurationGateway;
import com.smartsparrow.config.data.ConfigurationLoadCache;
import com.smartsparrow.config.data.ConfigurationType;

class ConfigurationServiceTest {


    @Mock
    private ConfigurationGateway configurationGateway;

    @Mock
    private ConfigurationCache cache;

    @InjectMocks
    private ConfigurationService configurationService;

    @Mock
    private Configuration configuration;

    @Mock
    private Map<String, Provider<Configuration>> boundConfiguration;

    @Mock
    Provider<Configuration> configurationProvider;

    @Mock
    Provider<ConfigurationLoadCache> loadCacheProvider;

    @Mock
    Map<ConfigurationType, Provider<ConfigurationLoadCache>> configurationTypeProvider;

    @Mock
    ConfigurationLoadCache configurationLoadCache;

    private static final String REGION = "AP_SOUTHEAST_2";
    private static final String KEY = "key";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getValueAsync() {
        configurationService.getValueAsync(KEY);
        verify(cache).get(eq(KEY), any(Callable.class));
    }

    @Test
    void get() {
        configurationService.get(Integer.class, KEY);
        verify(cache).get(eq(Integer.class.getName() + "-" + KEY), any(Callable.class));
    }

    //uncomment this once boundConfiguration object is being called from any config module.
  /*  @Test
    void load_fromCache() {
        when(boundConfiguration.get(KEY)).thenReturn(configurationProvider);
        when(configurationProvider.get()).thenReturn(configuration);
        when(configuration.getConfigType()).thenReturn(ConfigurationType.DYNAMIC);
        when(configurationTypeProvider.get(ConfigurationType.DYNAMIC)).thenReturn(loadCacheProvider);
        when(loadCacheProvider.get()).thenReturn(configurationLoadCache);

        when(configurationLoadCache.getIfPresent(KEY)).thenReturn(Integer.class);

        configurationService.load(KEY, Integer.class);
        verify(configurationLoadCache).getIfPresent(eq(KEY));
    }*/

    //uncomment this once boundConfiguration object is being called from any config module.
   /* @Test
    void load_fromS3Config() {
        when(boundConfiguration.get(KEY)).thenReturn(configurationProvider);
        when(configurationProvider.get()).thenReturn(configuration);
        when(configuration.getConfigType()).thenReturn(ConfigurationType.DYNAMIC);
        when(configurationTypeProvider.get(ConfigurationType.DYNAMIC)).thenReturn(loadCacheProvider);
        when(loadCacheProvider.get()).thenReturn(configurationLoadCache);

        when(configurationLoadCache.getIfPresent(KEY)).thenReturn(null);
        when(configuration.load(any(), any())).thenReturn(Integer.class);
        configurationService.load(KEY, Integer.class);

        verify(configurationLoadCache).getIfPresent(eq(KEY));
        verify(configuration).load(any(), any());
    }*/

}
