package com.smartsparrow.sso.service;

import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.sso.event.RegistrarSectionRoleGetEventMessage;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import static com.smartsparrow.dataevent.RouteUri.REGISTRAR_SECTION_ROLE_GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrarServiceTest {

    @InjectMocks
    private RegistrarService registrarService;

    @Mock
    private CamelReactiveStreamsService camel;

    private final static String userId = "some user id";
    private final static String sectionId = "some section id";
    private final static String token = "some token";
    private final static String role = "some role";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getSectionRole_invalid() {
        when(camel.toStream(eq(REGISTRAR_SECTION_ROLE_GET), any(RegistrarSectionRoleGetEventMessage.class), eq(RegistrarSectionRoleGetEventMessage.class)))
                .thenReturn(Mono.just(new RegistrarSectionRoleGetEventMessage(userId, sectionId, token)));

        UnauthorizedFault f = assertThrows(UnauthorizedFault.class, () -> registrarService.getSectionRole(userId, sectionId, token).block());

        assertNotNull(f);
        assertEquals("Invalid token supplied", f.getMessage());

        ArgumentCaptor<RegistrarSectionRoleGetEventMessage> captor = ArgumentCaptor
                .forClass(RegistrarSectionRoleGetEventMessage.class);

        verify(camel).toStream(eq(REGISTRAR_SECTION_ROLE_GET), captor.capture(), eq(RegistrarSectionRoleGetEventMessage.class));
    }

    @Test
    void getSectionRole_valid() {
        RegistrarSectionRoleGetEventMessage eventMessage = new RegistrarSectionRoleGetEventMessage(userId, sectionId, token);
        eventMessage.setSectionRole(new SectionRole().setId(sectionId).setRole(role));

        when(camel.toStream(eq(REGISTRAR_SECTION_ROLE_GET), any(RegistrarSectionRoleGetEventMessage.class), eq(RegistrarSectionRoleGetEventMessage.class)))
                .thenReturn(Mono.just(eventMessage));

        SectionRole sectionRole = registrarService.getSectionRole(userId, sectionId, token).block();

        assertNotNull(sectionRole);
        assertEquals(sectionId, sectionRole.getId());
        assertEquals(role, sectionRole.getRole());
    }
}
