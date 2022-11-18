package com.smartsparrow.rtm.message;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

import javax.inject.Inject;

import com.google.common.base.MoreObjects;
import com.smartsparrow.rtm.wiring.RTMScoped;
import com.smartsparrow.rtm.ws.RTMWebSocketContext;

/**
 * Expose the RTM client connection information in an immutable manner.
 *
 * This should be injected as a Provider&lt;RTMClientContext&gt;
 */
@RTMScoped
public class RTMClientContext implements Serializable {

    private static final long serialVersionUID = -3197171420377179243L;
    private final String clientId;
    private final InetAddress remoteAddress;
    private final RTMWebSocketContext rtmWebSocketContext;

    @Inject
    RTMClientContext() {
        clientId = null;
        remoteAddress = null;
        rtmWebSocketContext = null;
    }

    public RTMClientContext(final String clientId,
                            final InetAddress remoteAddress,
                            final RTMWebSocketContext rtmWebSocketContext) {
        this.clientId = clientId;
        this.remoteAddress = remoteAddress;
        this.rtmWebSocketContext = rtmWebSocketContext;
    }

    /**
     * Get the system generated client id.
     *
     * @return client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the remote address of the session.
     *
     * @return the remote address
     */
    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public RTMWebSocketContext getRtmWebSocketContext() {
        return rtmWebSocketContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RTMClientContext that = (RTMClientContext) o;
        return Objects.equals(clientId, that.clientId)
                && Objects.equals(rtmWebSocketContext, that.rtmWebSocketContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, rtmWebSocketContext);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("clientId", clientId)
                .add("rtmWebSocketContext", rtmWebSocketContext)
                .toString();
    }
}
