ALTER TABLE learner.grade_passback_notification_by_deployment
    ADD (
    courseware_element_id timeuuid,
    courseware_element_type text
    );