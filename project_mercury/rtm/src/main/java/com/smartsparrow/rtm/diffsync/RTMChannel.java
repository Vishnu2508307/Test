package com.smartsparrow.rtm.diffsync;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jetty.websocket.api.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import data.Channel;
import data.ChannelOperationFault;
import data.DiffSyncIdentifier;
import data.Exchangeable;
import data.Message;

/**
 * Channel type used for maintaining a diff sync with a WebSocket Client
 */
public class RTMChannel implements Channel {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RTMChannel.class);

    // the websocket session used for writing the message to the client
    private final Session session;
    // the clientId that initialized this channel
    private final String clientId;
    private final MessageTypeBridgeConverter messageTypeBridgeConverter;

    private static final ObjectMapper om = new ObjectMapper();

    /**
     * Package-private constructor to init an RTMChannel use {@link RTMChannelFactory#create(Session, String)}
     *
     * @param session the websocket session
     * @param clientId the clientId that initialized the websocket connection
     * @param messageTypeBridgeConverter the message type converter
     */
    RTMChannel(final Session session, final String clientId, final MessageTypeBridgeConverter messageTypeBridgeConverter) {
        this.session = session;
        this.clientId = clientId;
        this.messageTypeBridgeConverter = messageTypeBridgeConverter;
    }

    /**
     * Writes the {@link Message} on the WebSocket, attaching the proper {@link com.smartsparrow.rtm.message.MessageType}
     * based on the {@link Exchangeable.Type}
     *
     * @param message the message to send over the channel
     * @throws IllegalArgumentFault when the message body is invalid or unsupported
     */
    @Override
    public void send(Message<? extends Exchangeable> message) {
        // get the body of the message
        final Exchangeable body = message.getBody();
        // find the RTM message type based on the body type
        final String messageType = messageTypeBridgeConverter.from(body.getType());

        // convert the body to a map so that we can later add it to the
        // response message
        @SuppressWarnings("unchecked")
        Map<String, Object> bodyMap = om.convertValue(body, Map.class);
        try {
            // write the message on the websocket
            // TODO replace this with proper Messages from MessageHandlers definition in BRNT-13938/BRNT-13055
            Responses.write(session, new BasicResponseMessage(messageType, null)
                    .addAllFields(bodyMap)
            .addField("clientId", clientId));
        } catch (WriteResponseException e) {
            // if the message fails log the error
            log.jsonError("error writing message on diffsync RTMChannel", bodyMap, e);
            // throw the fault
            throw new ChannelOperationFault("error sending message to the RTMChannel");
        }

    }

    @Override
    public void receive(Message<? extends Exchangeable> message) {
        // since we have message handlers to handle incoming messages
        // the receive implementation might not be required TODO investigate
    }

    public Session getSession() {
        return session;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RTMChannel that = (RTMChannel) o;
        return Objects.equals(session, that.session)
                && Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, clientId);
    }

    @Override
    public String toString() {
        return "RTMChannel{" +
                "session=" + session +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
