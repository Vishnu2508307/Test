package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.iam.data.ClaimGateway;

import reactor.core.publisher.Flux;

class ClaimServiceTest {

    @Mock
    private ClaimGateway claimGateway;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ClaimService claimService;

    private Claim claim;
    private Account account;

    private UUID accountId = UUIDs.timeBased();
    private UUID subscriptionId = UUIDs.timeBased();
    private String name = "name";
    private String value = "value";
    private Region region = Region.GLOBAL;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        claimService = new ClaimService(accountService, claimGateway);

        claim = new Claim() 
                .setAccountId(accountId)
                .setIamRegion(region)
                .setSubscriptionId(subscriptionId)
                .setName(name)
                .setValue(value);

        account = new Account()
                .setId(accountId)
                .setIamRegion(region)
                .setSubscriptionId(subscriptionId);

        when(claimGateway.persist(claim)).thenReturn(Flux.just(new Void[]{}));
        when(accountService.findById(accountId)).thenReturn(Flux.just(account));
        when(claimGateway.find(region, accountId, subscriptionId)).thenReturn(Flux.just(claim));

    }

    @Test
    void add_test() {
        ArgumentCaptor<Claim> claimArgumentCaptor = ArgumentCaptor.forClass(Claim.class);
        claimService.add(accountId, subscriptionId, name, value).collectList().block();
        verify(claimGateway, atLeastOnce()).persist(claimArgumentCaptor.capture());
        Claim claimArgumentCaptorValue = claimArgumentCaptor.getValue();
        assertEquals(claimArgumentCaptorValue.getAccountId(), accountId);
        assertEquals(claimArgumentCaptorValue.getIamRegion(), region);
        assertEquals(claimArgumentCaptorValue.getSubscriptionId(), subscriptionId);
    }

    @Test
    void find_test() {
        Flux<Claim> fluxClaim = claimService.find(accountId, subscriptionId);
        List<Claim> listClaim = fluxClaim.collectList().block();
        assertNotNull(listClaim);
        assertEquals(1, listClaim.size());
        Claim claimResponse = listClaim.get(0);
        assertEquals(claim.getSubscriptionId(), claimResponse.getSubscriptionId());
        assertEquals(claim.getAccountId(), claimResponse.getAccountId());
        assertEquals(claim.getIamRegion(), claimResponse.getIamRegion());
        assertEquals(claim.getValue(), claimResponse.getValue());
        assertEquals(claim.getName(), claimResponse.getName());
    }
}