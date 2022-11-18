--
-- ext_http.result_notification
-- ----------------------------
-- Track the results of the HTTP Request
--
-- Find the response for a given request
-- > SELECT * FROM ext_http.result_notification WHERE notification_id = ?;
--
CREATE TABLE IF NOT EXISTS ext_http.result_notification (
    id timeuuid,
    notification_id timeuuid,
    sequence_id timeuuid,
    operation text,
    uri text,
    method text,
    headers frozen<map<text, list<text>>>,
    body text,
    status_code int,
    timing_start bigint,
    timing_perf_socket float,
    timing_perf_lookup float,
    timing_perf_connect float,
    timing_perf_response float,
    timing_perf_end float,
    timing_phase_duration_wait float,
    timing_phase_duration_dns float,
    timing_phase_duration_tcp float,
    timing_phase_duration_firstByte float,
    timing_phase_duration_download float,
    timing_phase_duration_total float,
    PRIMARY KEY ( notification_id, id, sequence_id )
)
WITH CLUSTERING ORDER BY (id DESC, sequence_id DESC)
 AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
