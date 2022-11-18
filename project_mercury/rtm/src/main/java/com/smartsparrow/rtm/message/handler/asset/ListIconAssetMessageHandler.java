package com.smartsparrow.rtm.message.handler.asset;


import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.ListIconAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ListIconAssetMessageHandler implements MessageHandler<ListIconAssetMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListIconAssetMessageHandler.class);

    public static final String AUTHOR_ICON_ASSET_LIST = "author.icon.asset.list";
    public static final String AUTHOR_ICON_ASSET_LIST_OK = "author.icon.asset.list.ok";
    public static final String AUTHOR_ICON_ASSET_LIST_ERROR = "author.icon.asset.list.error";

    private final BronteAssetService bronteAssetService;

    @Inject
    public ListIconAssetMessageHandler(final BronteAssetService bronteAssetService) {
        this.bronteAssetService = bronteAssetService;
    }

    @Override
    public void validate(ListIconAssetMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getIconLibraries(), "icon library is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ICON_ASSET_LIST)
    @Override
    public void handle(Session session, ListIconAssetMessage message) throws WriteResponseException {

        bronteAssetService.fetchIconAssetsByLibrary(message.getIconLibraries())
                .doOnEach(log.reactiveErrorThrowable("error fetching icon assets by library"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(iconAssetSummaries -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ICON_ASSET_LIST_OK,
                                       message.getId());
                               basicResponseMessage.addField("iconAssetSummaries", iconAssetSummaries);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to fetch icon assets by library ", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ICON_ASSET_LIST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to fetch icon assets by library");
                           }
                );
    }
}
