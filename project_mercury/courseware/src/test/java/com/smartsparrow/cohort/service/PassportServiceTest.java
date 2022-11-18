package com.smartsparrow.cohort.service;

import static com.smartsparrow.dataevent.RouteUri.IES_TOKEN_VALIDATE;
import static com.smartsparrow.dataevent.RouteUri.PASSPORT_ENTITLEMENT_CHECK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.eventmessage.PassportEntitlementEventMessage;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.exception.PermissionFault;

import reactor.core.publisher.Mono;

class PassportServiceTest {

    @InjectMocks
    private PassportService passportService;

    @Mock
    private CamelReactiveStreamsService camel;

    private static final String pearsonUid = "007";
    private static final String productURN = "x-urn:bronte:random-uuid";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void checkEntitlement_noPearsonUid() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> passportService.checkEntitlement(null, productURN));

        assertNotNull(f);
        assertEquals("pearsonUid is required", f.getMessage());
    }

    @Test
    void checkEntitlement_noProductURN() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> passportService.checkEntitlement(pearsonUid, null));

        assertNotNull(f);
        assertEquals("productURN is required", f.getMessage());
    }

    @Test
    void checkEntitlement_notEntitled() {
        when(camel.toStream(eq(PASSPORT_ENTITLEMENT_CHECK), any(PassportEntitlementEventMessage.class), eq(PassportEntitlementEventMessage.class)))
                .thenReturn(Mono.just(new PassportEntitlementEventMessage(pearsonUid, productURN)));

        PermissionFault f = assertThrows(PermissionFault.class,
                () -> passportService.checkEntitlement(pearsonUid, productURN)
                        .block());

        assertNotNull(f);
        assertEquals("not entitled", f.getMessage());

        verify(camel)
                .toStream(eq(PASSPORT_ENTITLEMENT_CHECK), any(PassportEntitlementEventMessage.class), eq(PassportEntitlementEventMessage.class));
    }

    @Test
    void checkEntitlement() {
        when(camel.toStream(eq(PASSPORT_ENTITLEMENT_CHECK), any(PassportEntitlementEventMessage.class), eq(PassportEntitlementEventMessage.class)))
                .thenReturn(Mono.just(new PassportEntitlementEventMessage(pearsonUid, productURN)
                        .grantAccess()));

        Boolean isEntitled = passportService.checkEntitlement(pearsonUid, productURN)
                .block();

        assertNotNull(isEntitled);
        assertTrue(isEntitled);

        verify(camel)
                .toStream(eq(PASSPORT_ENTITLEMENT_CHECK), any(PassportEntitlementEventMessage.class), eq(PassportEntitlementEventMessage.class));
    }

}