ALTER TABLE cohort.settings_by_cohort
    ADD (
        trial_duration_seconds int,
        trial_end_epoch_seconds bigint
    );

