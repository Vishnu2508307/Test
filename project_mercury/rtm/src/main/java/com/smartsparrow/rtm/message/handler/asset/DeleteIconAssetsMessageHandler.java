package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.DeleteIconAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;


public class DeleteIconAssetsMessageHandler implements MessageHandler<DeleteIconAssetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteIconAssetsMessageHandler.class);

    public static final String AUTHOR_ICON_ASSET_DELETE = "author.icon.asset.delete";
    public static final String AUTHOR_ICON_ASSET_DELETE_OK = "author.icon.asset.delete.ok";
    public static final String AUTHOR_ICON_ASSET_DELETE_ERROR = "author.icon.asset.delete.error";

    private final BronteAssetService bronteAssetService;

    @Inject
    public DeleteIconAssetsMessageHandler(BronteAssetService bronteAssetService) {
        this.bronteAssetService = bronteAssetService;
    }

    @Override
    public void validate(DeleteIconAssetMessage message) throws RTMValidationException {
        affirmArgument(message.getIconLibrary() != null, "missing icon library");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ICON_ASSET_DELETE)
    @Override
    public void handle(Session session,
                       DeleteIconAssetMessage message) throws WriteResponseException {
        bronteAssetService.deleteIconAssetsByLibrary(message.getIconLibrary())
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error icon assets by library",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("iconLibrary", message.getIconLibrary());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(ignore -> {
                               // nothing here, never executed
                           }, ex -> {
                               log.jsonDebug("Unable to delete icon assets by library", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ICON_ASSET_DELETE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "error deleting icon assets by library");
                           },
                           () -> Responses.writeReactive(session,
                                                         new BasicResponseMessage(AUTHOR_ICON_ASSET_DELETE_OK,
                                                                                  message.getId())));

    }
}
