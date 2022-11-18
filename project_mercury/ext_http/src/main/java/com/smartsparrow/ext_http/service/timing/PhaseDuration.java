package com.smartsparrow.ext_http.service.timing;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Contains the durations of each request phase.
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson using field reflection, obj is immutable conceptually")
public class PhaseDuration {

    private Float wait;
    private Float dns;
    private Float tcp;
    private Float firstByte;
    private Float download;
    private Float total;

    /**
     *
     * @return Duration of socket initialization (timings.socket)
     */
    public Float getWait() {
        return wait;
    }

    /**
     *
     * @return Duration of DNS lookup (timings.lookup - timings.socket)
     */
    public Float getDns() {
        return dns;
    }

    /**
     *
     * @return Duration of TCP connection (timings.connect - timings.socket)
     */
    public Float getTcp() {
        return tcp;
    }

    /**
     *
     * @return Duration of HTTP server response (timings.response - timings.connect)
     */
    public Float getFirstByte() {
        return firstByte;
    }

    /**
     *
     * @return Duration of HTTP download (timings.end - timings.response)
     */
    public Float getDownload() {
        return download;
    }

    /**
     *
     * @return Duration entire HTTP round-trip (timings.end)
     */
    public Float getTotal() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PhaseDuration that = (PhaseDuration) o;
        return Objects.equals(wait, that.wait) && Objects.equals(dns, that.dns) && Objects.equals(tcp, that.tcp)
                && Objects.equals(firstByte, that.firstByte) && Objects.equals(download, that.download)
                && Objects.equals(total, that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wait, dns, tcp, firstByte, download, total);
    }

    @Override
    public String toString() {
        return "PhaseDuration{" + "wait=" + wait + ", dns=" + dns + ", tcp=" + tcp + ", firstByte=" + firstByte
                + ", download=" + download + ", total=" + total + '}';
    }
}
