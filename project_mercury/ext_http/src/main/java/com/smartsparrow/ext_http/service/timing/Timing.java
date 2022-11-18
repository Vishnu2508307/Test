package com.smartsparrow.ext_http.service.timing;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Timing metrics of the request-response cycle (including all redirects) is timed at millisecond resolution.
 *
 * If there were redirects, the properties reflect the timings of the final request in the redirect chain.
 *
 * <pre>
 *   "time": {
 *      "timingStart": 1558418803191,
 *      "timings": {
 *         "socket": 0.37057499999991705,
 *         "lookup": 1.235776999999871,
 *         "connect": 225.3632150000003,
 *         "response": 471.57646100000056,
 *         "end": 471.76921500000026
 *      },
 *      "timingPhases": {
 *         "wait": 0.37057499999991705,
 *         "dns": 0.8652019999999538,
 *         "tcp": 224.12743800000044,
 *         "firstByte": 246.21324600000025,
 *         "download": 0.19275399999969522,
 *         "total": 471.76921500000026
 *      }
 *   }
 * </pre>
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson using field reflection, obj is immutable conceptually")
public class Timing {

    private Long timingStart;
    private Performance timings;
    private PhaseDuration timingPhases;

    /**
     *
     * @return the Unix epoch millis of the start of the request
     */
    public Long getTimingStart() {
        return timingStart;
    }

    public Performance getTimings() {
        return timings;
    }

    public PhaseDuration getTimingPhases() {
        return timingPhases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Timing timing = (Timing) o;
        return Objects.equals(timingStart, timing.timingStart) && Objects.equals(timings, timing.timings)
                && Objects.equals(timingPhases, timing.timingPhases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timingStart, timings, timingPhases);
    }

    @Override
    public String toString() {
        return "Timing{" + "timingStart=" + timingStart + ", timings=" + timings + ", timingPhases=" + timingPhases
                + '}';
    }
}
