package com.smartsparrow.ext_http.service.timing;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Event timestamps recorded in millisecond resolution relative to timingStart
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson using field reflection, obj is immutable conceptually")
public class Performance {

    private Float socket;
    private Float lookup;
    private Float connect;
    private Float response;
    private Float end;

    /**
     *
     * @return Relative timestamp when the http module's socket event fires. This happens when the socket is assigned to the request.
     */
    public Float getSocket() {
        return socket;
    }

    /**
     *
     * @return Relative timestamp when the net module's lookup event fires. This happens when the DNS has been resolved.
     */
    public Float getLookup() {
        return lookup;
    }

    /**
     *
     * @return Relative timestamp when the net module's connect event fires. This happens when the server acknowledges the TCP connection.
     */
    public Float getConnect() {
        return connect;
    }

    /**
     *
     * @return Relative timestamp when the http module's response event fires. This happens when the first bytes are received from the server.
     */
    public Float getResponse() {
        return response;
    }

    /**
     *
     * @return Relative timestamp when the last bytes of the response are received.
     */
    public Float getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Performance that = (Performance) o;
        return Objects.equals(socket, that.socket) && Objects.equals(lookup, that.lookup) && Objects.equals(connect,
                                                                                                            that.connect)
                && Objects.equals(response, that.response) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, lookup, connect, response, end);
    }

    @Override
    public String toString() {
        return "TimingValues{" + "socket=" + socket + ", lookup=" + lookup + ", connect=" + connect + ", response="
                + response + ", end=" + end + '}';
    }
}
