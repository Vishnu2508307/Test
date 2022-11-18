package com.smartsparrow.rtm.wiring;

import static com.smartsparrow.rtm.message.handler.LogoutMessageHandler.ME_LOGOUT;
import static com.smartsparrow.rtm.message.handler.MeMessageHandler.ME_GET;
import static com.smartsparrow.rtm.message.handler.PingMessageHandler.PING;
import static com.smartsparrow.rtm.message.handler.iam.AuthenticateMessageHandler.AUTHENTICATE;

import java.util.HashMap;

import com.smartsparrow.data.AbstractModuleDecorator;
import com.smartsparrow.rtm.message.authorization.AllowAuthenticated;
import com.smartsparrow.rtm.message.authorization.Everyone;
import com.smartsparrow.rtm.message.handler.LogoutMessageHandler;
import com.smartsparrow.rtm.message.handler.MeMessageHandler;
import com.smartsparrow.rtm.message.handler.PingMessageHandler;
import com.smartsparrow.rtm.message.handler.iam.AuthenticateMessageHandler;
import com.smartsparrow.rtm.message.recv.LogoutMessage;
import com.smartsparrow.rtm.message.recv.MeMessage;
import com.smartsparrow.rtm.message.recv.PingMessage;
import com.smartsparrow.rtm.message.recv.iam.AuthenticateMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractRTMOperationsModule extends AbstractModuleDecorator {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AbstractRTMOperationsModule.class);

    protected RTMMessageOperations binder;

    @Override
    public abstract void decorate();

    @SuppressFBWarnings(value = "DM_EXIT", justification = "Shut down the application if the default binding fails")
    @Override
    protected void configure() {
        binder = new RTMMessageOperations(binder());

        try {
            // bind messages that are common to workspace and learnspace
            binder.bind(PING) //
                    .toMessageType(PingMessage.class) //
                    .withAuthorizers(Everyone.class) //
                    .withMessageHandlers(PingMessageHandler.class);

            binder.bind(AUTHENTICATE) //
                    .toMessageType(AuthenticateMessage.class) //
                    .withAuthorizers(Everyone.class) //
                    .withMessageHandlers(AuthenticateMessageHandler.class);

            binder.bind(ME_GET) //
                    .toMessageType(MeMessage.class) //
                    .withAuthorizers(AllowAuthenticated.class) //
                    .withMessageHandlers(MeMessageHandler.class);

            binder.bind(ME_LOGOUT) //
                    .toMessageType(LogoutMessage.class) //
                    .withAuthorizers(AllowAuthenticated.class) //
                    .withMessageHandlers(LogoutMessageHandler.class);

            // use the decorator to add any additional binding based on the instance type
            this.decorate();
        } catch (RTMMessageBindingException e) {
            // if the binding fails exit
            log.jsonError(e.getMessage(), new HashMap<>(), e);
            System.exit(1);
        }
    }
}
