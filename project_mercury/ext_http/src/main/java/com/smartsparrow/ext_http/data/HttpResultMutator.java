package com.smartsparrow.ext_http.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.ext_http.service.HttpEvent;
import com.smartsparrow.ext_http.service.HttpResult;
import com.smartsparrow.ext_http.service.timing.Timing;
import com.smartsparrow.util.Enums;

class HttpResultMutator extends SimpleTableMutator<HttpResult> {

    @Override
    public String getUpsertQuery(final HttpResult mutation) {
        // @formatter:off
        return "INSERT INTO ext_http.result_notification ("
                + "  id"
                + ", notification_id"
                + ", sequence_id"
                + ", operation"
                + ", uri"
                + ", method"
                + ", headers"
                + ", body"
                + ", status_code"
                + ", timing_start"
                + ", timing_perf_socket"
                + ", timing_perf_lookup"
                + ", timing_perf_connect"
                + ", timing_perf_response"
                + ", timing_perf_end"
                + ", timing_phase_duration_wait"
                + ", timing_phase_duration_dns"
                + ", timing_phase_duration_tcp"
                + ", timing_phase_duration_firstByte"
                + ", timing_phase_duration_download"
                + ", timing_phase_duration_total"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + "           ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + "           ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final HttpResult mutation) {
        HttpEvent event = mutation.getEvent();

        stmt.setUUID(0, mutation.getId())
                .setUUID(1, mutation.getNotificationId())
                .setUUID(2, mutation.getSequenceId())
                .setString(3, Enums.asString(event.getOperation()))
                .setString(4, event.getUri())
                .setString(5, event.getMethod())
                .setMap(6, event.getHeaders())
                .setString(7, event.getBody());
        if(event.getStatusCode() != null) {
            stmt.setInt(8, event.getStatusCode());
        }
        Timing timing = mutation.getEvent().getTime();
        if (timing != null) {
            stmt.setLong(9, timing.getTimingStart())
                    .setFloat(10, timing.getTimings().getSocket())
                    .setFloat(11, timing.getTimings().getLookup())
                    .setFloat(12, timing.getTimings().getConnect())
                    .setFloat(13, timing.getTimings().getResponse())
                    .setFloat(14, timing.getTimings().getEnd())
                    .setFloat(15, timing.getTimingPhases().getWait())
                    .setFloat(16, timing.getTimingPhases().getDns())
                    .setFloat(17, timing.getTimingPhases().getTcp())
                    .setFloat(18, timing.getTimingPhases().getFirstByte())
                    .setFloat(19, timing.getTimingPhases().getDownload())
                    .setFloat(20, timing.getTimingPhases().getTotal());
        }
    }

}
