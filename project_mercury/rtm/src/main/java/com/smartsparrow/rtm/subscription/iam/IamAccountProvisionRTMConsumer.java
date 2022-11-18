package com.smartsparrow.rtm.subscription.iam;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;

/**
 * Consumer that handles the iam provision account RTM event
 */
public class IamAccountProvisionRTMConsumer implements RTMConsumer<IamAccountProvisionRTMConsumable> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IamAccountProvisionRTMConsumer.class);

    private AccountService accountService;

    @Inject
    public IamAccountProvisionRTMConsumer(AccountService accountService){
        this.accountService = accountService;
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new IamAccountProvisionRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the subscription id and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param iamAccountProvisionRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, IamAccountProvisionRTMConsumable iamAccountProvisionRTMConsumable) {
        final RTMClientContext producingRTMClientContext = iamAccountProvisionRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            IamAccountBroadcastMessage message = iamAccountProvisionRTMConsumable.getContent();
            final String broadcastType = iamAccountProvisionRTMConsumable.getBroadcastType();
            final UUID subscriptionId = iamAccountProvisionRTMConsumable.getSubscriptionId();

            accountService.getAccountPayload(message.getAccountId()).subscribe(accountPayload -> {
                try {
                    Responses.writeReactive(rtmClient.getSession(),
                                            new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                                    .addField("account", accountPayload)
                                                    .addField("rtmEvent", getRTMEvent().getName()));
                } catch (Exception e) {
                    log.jsonError("Failed broadcasting to client", new HashMap<String, Object>() {
                        {
                            put("clientId", rtmClient.getRtmClientContext().getClientId());
                        }
                    }, e);
                }

            }, throwable -> {
                throwable = Exceptions.unwrap(throwable);
                String errorMessage = String.format("Error fetching account %s", subscriptionId);
                log.jsonError(errorMessage, new HashMap<String, Object>() {
                    {
                        put("clientId", rtmClient.getRtmClientContext().getClientId());
                    }
                }, throwable);
                Responses.errorReactive(rtmClient.getSession(),
                                        null,
                                        broadcastType + ".error",
                                        HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                        errorMessage);
            });

        }
    }
}


