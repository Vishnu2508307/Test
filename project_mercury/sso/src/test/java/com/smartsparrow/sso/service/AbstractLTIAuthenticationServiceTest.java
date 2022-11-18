package com.smartsparrow.sso.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.WebSession;

import reactor.core.publisher.Mono;

@SuppressWarnings("rawtypes")
class AbstractLTIAuthenticationServiceTest {

    @InjectMocks
    private AbstractLTIAuthenticationService abstractLTIAuthenticationService;

    @Mock
    private AuthenticationService lti11AuthenticationService;

    @Mock
    private LTIConsumerCredentials credentials;

    @Mock
    private WebSession webSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @SuppressWarnings("unchecked")
    @Test
    void authenticate_lti11() {
        when(credentials.getLTIVersion()).thenReturn(LTIVersion.VERSION_1_1);

        when(lti11AuthenticationService.authenticate(credentials))
                .thenReturn(Mono.just(webSession));

        abstractLTIAuthenticationService.authenticate(credentials)
                .block();

        verify(lti11AuthenticationService).authenticate(credentials);
    }

}