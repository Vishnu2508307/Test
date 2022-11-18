-- Adding payment and ttl columns
ALTER TABLE cohort_enrollment.enrollment_by_cohort
    ADD (
    payment_method text,
    payment_id timeuuid,
    ttl_seconds int,
    expires_at text,
    enrolled_at text,
    enrolled_by timeuuid
    );
