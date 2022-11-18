/*
**************************
*** Drop unused tables ***
**************************
 */
DROP TABLE IF EXISTS asset.asset_by_user;
DROP TABLE IF EXISTS cohort.adjustment_grade;
DROP TABLE IF EXISTS cohort.adjustment_grade_by_student;
DROP TABLE IF EXISTS cohort.alg_bkt_by_bank;
DROP TABLE IF EXISTS cohort.deployment_by_cohort;
DROP TABLE IF EXISTS cohort.deployment_latest_score_percentage_by_screen;
DROP TABLE IF EXISTS cohort.deployment_latest_screen_attempt_by_screen;
DROP TABLE IF EXISTS cohort.deployment_latest_time_spent_by_screen;
DROP TABLE IF EXISTS cohort.deployment_location_by_student_open_time;
DROP TABLE IF EXISTS cohort.deployment_progress;
DROP TABLE IF EXISTS cohort.deployment_progress_finished;
DROP TABLE IF EXISTS cohort.deployment_score_by_cohort;
DROP TABLE IF EXISTS cohort.deployment_student_by_screen;
DROP TABLE IF EXISTS cohort.extension_by_cohort_deployment;
DROP TABLE IF EXISTS cohort.leaderboard_moniker_by_account;
DROP TABLE IF EXISTS cohort.learnspace_deployment_by_student;
DROP TABLE IF EXISTS cohort.location_by_owner;
DROP TABLE IF EXISTS cohort.manual_grade_by_deployment;
DROP TABLE IF EXISTS cohort.max_progress_by_student_deployment;
DROP TABLE IF EXISTS cohort.student_deployment_progress;
DROP TABLE IF EXISTS cohort.student_enrollment_by_cohort;
DROP TABLE IF EXISTS cohort.student_enrollment_by_cohort_deployment;
DROP TABLE IF EXISTS data.enrollment_student_by_deployment;
DROP TABLE IF EXISTS data.enrollment_student_by_lesson;
DROP TABLE IF EXISTS data.lesson_by_student;
DROP TABLE IF EXISTS iam_global.saml_account_by_name_id;
DROP TABLE IF EXISTS iam_global.saml_client_config_by_id;
DROP TABLE IF EXISTS iam_global.saml_client_config_by_url_id;
DROP TABLE IF EXISTS iam_global.saml_name_id_by_account;
DROP TABLE IF EXISTS iam_global.saml_session;
DROP TABLE IF EXISTS iam_global.saml_session_by_account;
DROP TABLE IF EXISTS iam_global.saml_session_by_auth_token;
DROP TABLE IF EXISTS iam_global.saml_session_log;
DROP TABLE IF EXISTS ip.ip_info;
DROP TABLE IF EXISTS lesson.activation_latest_score_percentage_by_screen;
DROP TABLE IF EXISTS lesson.activation_latest_screen_attempt_by_screen;
DROP TABLE IF EXISTS lesson.activation_latest_time_spent_by_screen;
DROP TABLE IF EXISTS lesson.activation_progress;
DROP TABLE IF EXISTS lesson.activation_student_by_screen;
DROP TABLE IF EXISTS lesson.alg_bkt_by_bank;
DROP TABLE IF EXISTS lesson.answer_component_by_lesson;
DROP TABLE IF EXISTS lesson.author_warning;
DROP TABLE IF EXISTS lesson.copied_from_lesson;
DROP TABLE IF EXISTS lesson.copied_parent_lesson;
DROP TABLE IF EXISTS lesson.location_by_owner;
DROP TABLE IF EXISTS lesson.location_by_student;
DROP TABLE IF EXISTS lesson.location_by_student_open_time;
DROP TABLE IF EXISTS lesson.manual_grading_component_by_lesson;
DROP TABLE IF EXISTS lesson.max_progress_by_student_activation;
DROP TABLE IF EXISTS lesson.metadata_by_lesson;
DROP TABLE IF EXISTS lesson.screenshot_by_lesson;
DROP TABLE IF EXISTS lesson.screenshot_map_to_legacy_id;
DROP TABLE IF EXISTS lesson.tmp_map_progress;
DROP TABLE IF EXISTS lti.batch_cohort_grade_replace;
DROP TABLE IF EXISTS lti.batch_lesson_grade_replace;
DROP TABLE IF EXISTS report.accounts_by_domain;
DROP TABLE IF EXISTS report.all_time_instructors_deployed_lesson;
DROP TABLE IF EXISTS report.all_time_instructors_deployed_lesson_five_plus;
DROP TABLE IF EXISTS report.all_time_instructors_deployed_student;
DROP TABLE IF EXISTS report.all_time_instructors_deployed_student_five_plus;
DROP TABLE IF EXISTS report.fy_instructors_deployed_lesson;
DROP TABLE IF EXISTS report.fy_instructors_deployed_lesson_five_plus;
DROP TABLE IF EXISTS report.fy_instructors_deployed_to_student;
DROP TABLE IF EXISTS report.fy_instructors_deployed_to_student_five_plus;
DROP TABLE IF EXISTS report.hy_instructors_deployed_lesson;
DROP TABLE IF EXISTS report.hy_instructors_deployed_lesson_five_plus;
DROP TABLE IF EXISTS report.hy_instructors_deployed_to_student;
DROP TABLE IF EXISTS report.hy_instructors_deployed_to_student_five_plus;
DROP TABLE IF EXISTS report.instructor_activity;
DROP TABLE IF EXISTS report.instructor_signup_by_month;
DROP TABLE IF EXISTS report.lesson_use_by_year_bucketed_by_student_count;
DROP TABLE IF EXISTS report.lessons_by_year;
DROP TABLE IF EXISTS report.monthly_cumulative_deployed_lesson;
DROP TABLE IF EXISTS report.monthly_cumulative_deployed_lesson_five_plus;
DROP TABLE IF EXISTS report.monthly_cumulative_unique_student_by_domain;
DROP TABLE IF EXISTS report.report_by_time;
DROP TABLE IF EXISTS report.student_interaction;
DROP TABLE IF EXISTS report.student_interaction_by_year;
DROP TABLE IF EXISTS report.unique_students;
DROP TABLE IF EXISTS report.unique_students_by_year;
DROP TABLE IF EXISTS report.weekly_instructors_deployed_lesson;
DROP TABLE IF EXISTS report.weekly_instructors_deployed_lesson_five_plus;
DROP TABLE IF EXISTS report.weekly_instructors_deployed_student;
DROP TABLE IF EXISTS report.weekly_instructors_deployed_student_five_plus;
DROP TABLE IF EXISTS riak.cntr;
DROP TABLE IF EXISTS riak.idx;
DROP TABLE IF EXISTS riak.key;
DROP TABLE IF EXISTS riak.kv;
DROP TABLE IF EXISTS sim.sim_data_by_student;
DROP TABLE IF EXISTS sim.sim_data_by_student_sim_key;
DROP TABLE IF EXISTS student_enrollment.course_by_student;
DROP TABLE IF EXISTS student_enrollment.lesson_by_student;
DROP TABLE IF EXISTS student_enrollment.student_by_course;
DROP TABLE IF EXISTS student_enrollment.student_by_lesson;
DROP TABLE IF EXISTS student_interaction.id_by_cohort_deployment;
DROP TABLE IF EXISTS student_interaction.id_by_cohort_deployment_screen;
DROP TABLE IF EXISTS student_interaction.id_by_cohort_deployment_student;
DROP TABLE IF EXISTS student_interaction.id_by_cohort_deployment_student_screen;
DROP TABLE IF EXISTS student_interaction.id_by_historical_id;
DROP TABLE IF EXISTS student_interaction.id_by_lesson;
DROP TABLE IF EXISTS student_interaction.id_by_lesson_screen;
DROP TABLE IF EXISTS student_interaction.id_by_lesson_student;
DROP TABLE IF EXISTS student_interaction.id_by_lesson_student_screen;
DROP TABLE IF EXISTS student_interaction.raw;
DROP TABLE IF EXISTS telemetry.active_lesson_by_instructor;
DROP TABLE IF EXISTS telemetry.active_lesson_by_instructor_hy;
DROP TABLE IF EXISTS telemetry.active_lesson_by_instructor_woy;
DROP TABLE IF EXISTS telemetry.active_lesson_by_student;
DROP TABLE IF EXISTS telemetry.active_lesson_by_student_hy;
DROP TABLE IF EXISTS telemetry.active_lesson_by_student_woy;
DROP TABLE IF EXISTS telemetry.active_student_by_instructor;
DROP TABLE IF EXISTS telemetry.active_student_by_instructor_hy;
DROP TABLE IF EXISTS telemetry.active_student_by_instructor_woy;
DROP TABLE IF EXISTS telemetry.active_student_by_lesson;
DROP TABLE IF EXISTS telemetry.active_student_by_lesson_hy;
DROP TABLE IF EXISTS telemetry.active_student_by_lesson_woy;
DROP TABLE IF EXISTS telemetry.active_student_by_ym;
DROP TABLE IF EXISTS telemetry.active_student_by_ymd;
DROP TABLE IF EXISTS telemetry.cohort_progress;
DROP TABLE IF EXISTS telemetry.deployment_open;
DROP TABLE IF EXISTS telemetry.learnspace_open;
DROP TABLE IF EXISTS telemetry.lesson_open;
DROP TABLE IF EXISTS telemetry.student_activity;
DROP TABLE IF EXISTS version.widget;
DROP TABLE IF EXISTS version.widget_summary;

/*
*****************************
*** Drop unused keyspaces ***
*****************************
 */
DROP KEYSPACE IF EXISTS data;
DROP KEYSPACE IF EXISTS lesson;
DROP KEYSPACE IF EXISTS lti;
DROP KEYSPACE IF EXISTS report;
DROP KEYSPACE IF EXISTS riak;
DROP KEYSPACE IF EXISTS sim;
DROP KEYSPACE IF EXISTS student_enrollment;
DROP KEYSPACE IF EXISTS student_interaction;
DROP KEYSPACE IF EXISTS telemetry;
DROP KEYSPACE IF EXISTS version;

/*
*****************************
*** Remove from changelog ***
*****************************
 */

DELETE FROM changelog.db where id = '20151030182806'; // { on_keyspace: student_interaction, on_table: raw }
DELETE FROM changelog.db where id = '20151113180142'; // { on_keyspace: telemetry, on_table: active_lesson_by_student }
DELETE FROM changelog.db where id = '20151116115257'; // { on_keyspace: telemetry, on_table: active_lesson_by_student_woy }
DELETE FROM changelog.db where id = '20151116131636'; // { on_keyspace: telemetry, on_table: active_lesson_by_instructor }
DELETE FROM changelog.db where id = '20151116133803'; // { on_keyspace: telemetry, on_table: active_lesson_by_instructor_hy }
DELETE FROM changelog.db where id = '20151116135758'; // { on_keyspace: telemetry, on_table: active_lesson_by_instructor_woy }
DELETE FROM changelog.db where id = '20151116202655'; // { on_keyspace: telemetry, on_table: active_lesson_by_student_hy }
DELETE FROM changelog.db where id = '20151116212257'; // { on_keyspace: telemetry, on_table: active_student_by_instructor }
DELETE FROM changelog.db where id = '20151116212305'; // { on_keyspace: telemetry, on_table: active_student_by_instructor_hy }
DELETE FROM changelog.db where id = '20151116212310'; // { on_keyspace: telemetry, on_table: active_student_by_instructor_woy }
DELETE FROM changelog.db where id = '20151116212320'; // { on_keyspace: telemetry, on_table: active_student_by_lesson }
DELETE FROM changelog.db where id = '20151116212325'; // { on_keyspace: telemetry, on_table: active_student_by_lesson_hy }
DELETE FROM changelog.db where id = '20151116212329'; // { on_keyspace: telemetry, on_table: active_student_by_lesson_woy }
DELETE FROM changelog.db where id = '20151210212851'; // { on_keyspace: student_enrollment, on_table: lesson_by_student }
DELETE FROM changelog.db where id = '20151210212902'; // { on_keyspace: student_enrollment, on_table: student_by_lesson }
DELETE FROM changelog.db where id = '20160111165127'; // { on_keyspace: student_enrollment, on_table: student_by_course }
DELETE FROM changelog.db where id = '20160111231827'; // { on_keyspace: student_enrollment, on_table: cohort_by_student }
DELETE FROM changelog.db where id = '20160111231850'; // { on_keyspace: student_enrollment, on_table: student_by_cohort }
DELETE FROM changelog.db where id = '20160121171235'; // { on_keyspace: student_enrollment, on_table: course_by_student }
DELETE FROM changelog.db where id = '20160301142606'; // { on_keyspace: cohort, on_table: student_enrollment_by_cohort_deployment }
DELETE FROM changelog.db where id = '20160301144507'; // { on_keyspace: cohort, on_table: extension_by_cohort_deployment }
DELETE FROM changelog.db where id = '20160301145343'; // { on_keyspace: cohort, on_table: deployment_meta_by_cohort }
DELETE FROM changelog.db where id = '20160302091155'; // { on_keyspace: cohort, on_table: student_enrollment_by_cohort }
DELETE FROM changelog.db where id = '20160309121349'; // { on_keyspace: riak, on_table: kv }
DELETE FROM changelog.db where id = '20160315153529'; // { on_keyspace: riak, on_table: idx }
DELETE FROM changelog.db where id = '20160315181310'; // { on_keyspace: riak, on_table: key }
DELETE FROM changelog.db where id = '20160317160451'; // { on_keyspace: riak, on_table: cntr }
DELETE FROM changelog.db where id = '20160517152354'; // { on_keyspace: student_enrollment, on_table: cohort_by_student_drop }
DELETE FROM changelog.db where id = '20160517152406'; // { on_keyspace: student_enrollment, on_table: student_by_cohort_drop }
DELETE FROM changelog.db where id = '20160527122055'; // { on_keyspace: telemetry, on_table: lesson_open }
DELETE FROM changelog.db where id = '20160527130906'; // { on_keyspace: telemetry, on_table: deployment_open }
DELETE FROM changelog.db where id = '20160527131109'; // { on_keyspace: telemetry, on_table: learnspace_open }
DELETE FROM changelog.db where id = '20160602152058'; // { on_keyspace: cohort, on_table: learnspace_deployment_by_student }
DELETE FROM changelog.db where id = '20160614133936'; // { on_keyspace: version, on_table: widget_summary }
DELETE FROM changelog.db where id = '20160621112532'; // { on_keyspace: version, on_table: widget }
DELETE FROM changelog.db where id = '20160729120450'; // { on_keyspace: lti, on_table: batch_cohort_grade_report }
DELETE FROM changelog.db where id = '20160818152941'; // { on_keyspace: lti, on_table: batch_lesson_grade_report }
DELETE FROM changelog.db where id = '20161004120321'; // { on_keyspace: version, on_table: widget_summary_alter }
DELETE FROM changelog.db where id = '20161004120700'; // { on_keyspace: version, on_table: widget_summary_alter }
DELETE FROM changelog.db where id = '20161005150350'; // { on_keyspace: version, on_table: widget_summary_alter }
DELETE FROM changelog.db where id = '20161013170629'; // { on_keyspace: riak, on_table: idx_lesson_progress }
DELETE FROM changelog.db where id = '20161104112326'; // { on_keyspace: lesson, on_table: screenshot_by_lesson }
DELETE FROM changelog.db where id = '20161115122516'; // { on_keyspace: lesson, on_table: screenshot_map_to_legacy_id }
DELETE FROM changelog.db where id = '20161121211552'; // { on_keyspace: sim, on_table: data_by_student_sim }
DELETE FROM changelog.db where id = '20161128134409'; // { on_keyspace: cohort, on_table: adjustment_grade_by_student }
DELETE FROM changelog.db where id = '20161203235116'; // { on_keyspace: lesson, on_table: answer_component_by_lesson }
DELETE FROM changelog.db where id = '20161207173841'; // { on_keyspace: data, on_table: lesson_by_student }
DELETE FROM changelog.db where id = '20161212153737'; // { on_keyspace: data, on_table: enrollment_student_by_lesson }
DELETE FROM changelog.db where id = '20161212162446'; // { on_keyspace: data, on_table: enrollment_student_by_deployment }
DELETE FROM changelog.db where id = '20161227181948'; // { on_keyspace: asset, on_table: asset_by_user }
DELETE FROM changelog.db where id = '20170120165138'; // { on_keyspace: cohort, on_table: deployment_meta_by_cohort_alter }
DELETE FROM changelog.db where id = '20170306192143'; // { on_keyspace: cohort, on_table: deployment_progress }
DELETE FROM changelog.db where id = '20170314184258'; // { on_keyspace: lesson, on_table: activation_progress }
DELETE FROM changelog.db where id = '20170319000027'; // { on_keyspace: lesson, on_table: activation_latest_score_percentage_by_screen }
DELETE FROM changelog.db where id = '20170319000029'; // { on_keyspace: lesson, on_table: activation_latest_time_spent_by_screen }
DELETE FROM changelog.db where id = '20170319000030'; // { on_keyspace: lesson, on_table: activation_latest_screen_attempt_by_screen }
DELETE FROM changelog.db where id = '20170319000032'; // { on_keyspace: lesson, on_table: activation_student_by_screen }
DELETE FROM changelog.db where id = '20170319000033'; // { on_keyspace: cohort, on_table: deployment_latest_score_percentage_by_screen }
DELETE FROM changelog.db where id = '20170319000035'; // { on_keyspace: cohort, on_table: deployment_latest_time_spent_by_screen }
DELETE FROM changelog.db where id = '20170319000036'; // { on_keyspace: cohort, on_table: deployment_latest_screen_attempt_by_screen }
DELETE FROM changelog.db where id = '20170319000038'; // { on_keyspace: cohort, on_table: deployment_student_by_screen }
DELETE FROM changelog.db where id = '20170330121727'; // { on_keyspace: lesson, on_table: activation_latest_time_spent_by_screen_alter }
DELETE FROM changelog.db where id = '20170330122851'; // { on_keyspace: lesson, on_table: activation_latest_time_spent_by_screen_alter }
DELETE FROM changelog.db where id = '20170330123029'; // { on_keyspace: cohort, on_table: deployment_latest_time_spent_by_screen_alter }
DELETE FROM changelog.db where id = '20170330123033'; // { on_keyspace: cohort, on_table: deployment_latest_time_spent_by_screen_alter }
DELETE FROM changelog.db where id = '20170414234743'; // { on_keyspace: riak, on_table: idx_lesson_progress_alter }
DELETE FROM changelog.db where id = '20170505143630'; // { on_keyspace: cohort, on_table: deployment_progress_finished }
DELETE FROM changelog.db where id = '20170517154727'; // { on_keyspace: lesson, on_table: author_warning }
DELETE FROM changelog.db where id = '20170623122203'; // { on_keyspace: cohort, on_table: manual_grade_by_deployment }
DELETE FROM changelog.db where id = '20170705111955'; // { on_keyspace: lesson, on_table: manual_grading_component_by_lesson }
DELETE FROM changelog.db where id = '20170728115721'; // { on_keyspace: cohort, on_table: manual_grade_by_deployment_alter }
DELETE FROM changelog.db where id = '20170728142038'; // { on_keyspace: cohort, on_table: manual_grade_by_deployment }
DELETE FROM changelog.db where id = '20170821102424'; // { on_keyspace: lesson, on_table: metadata_by_lesson }
DELETE FROM changelog.db where id = '20170822155704'; // { on_keyspace: telemetry, on_table: active_student_by_ym }
DELETE FROM changelog.db where id = '20170822155708'; // { on_keyspace: telemetry, on_table: active_student_by_ymd }
DELETE FROM changelog.db where id = '20170822162300'; // { on_keyspace: student_interaction, on_table: raw_alter }
DELETE FROM changelog.db where id = '20170822162301'; // { on_keyspace: student_interaction, on_table: raw_alter }
DELETE FROM changelog.db where id = '20170822162302'; // { on_keyspace: student_interaction, on_table: raw_alter }
DELETE FROM changelog.db where id = '20170906123722'; // { on_keyspace: lesson, on_table: manual_grading_component_by_lesson_alter }
DELETE FROM changelog.db where id = '20170919132030'; // { on_keyspace: sim, on_table: sim_data_by_student }
DELETE FROM changelog.db where id = '20171103205814'; // { on_keyspace: sim, on_table: data_by_student_sim_alter }
DELETE FROM changelog.db where id = '20171120114619'; // { on_keyspace: report, on_table: report_by_time }
DELETE FROM changelog.db where id = '20171120131636'; // { on_keyspace: report, on_table: unique_students }
DELETE FROM changelog.db where id = '20171121193702'; // { on_keyspace: student_interaction, on_table: raw_alter }
DELETE FROM changelog.db where id = '20171121201852'; // { on_keyspace: student_interaction, on_table: by_cohort_deployment }
DELETE FROM changelog.db where id = '20171121201910'; // { on_keyspace: student_interaction, on_table: by_lesson }
DELETE FROM changelog.db where id = '20171122185303'; // { on_keyspace: report, on_table: student_interaction }
DELETE FROM changelog.db where id = '20171127095023'; // { on_keyspace: telemetry, on_table: cohort_progress }
DELETE FROM changelog.db where id = '20171127115420'; // { on_keyspace: cohort, on_table: deployment_score_by_cohort }
DELETE FROM changelog.db where id = '20171129134520'; // { on_keyspace: cohort, on_table: student_deployment_progress }
DELETE FROM changelog.db where id = '20171201154048'; // { on_keyspace: cohort, on_table: deployment_by_cohort }
DELETE FROM changelog.db where id = '20171204191933'; // { on_keyspace: cohort, on_table: adjustment_grade }
DELETE FROM changelog.db where id = '20180111112335'; // { on_keyspace: report, on_table: accounts_by_domain }
DELETE FROM changelog.db where id = '20180111115604'; // { on_keyspace: report, on_table: student_interaction_by_year }
DELETE FROM changelog.db where id = '20180111121926'; // { on_keyspace: report, on_table: unique_students_by_year }
DELETE FROM changelog.db where id = '20180111133336'; // { on_keyspace: report, on_table: lessons_by_year }
DELETE FROM changelog.db where id = '20180111143142'; // { on_keyspace: report, on_table: instructor_activity }
DELETE FROM changelog.db where id = '20180111144314'; // { on_keyspace: report, on_table: instructor_signup_by_month }
DELETE FROM changelog.db where id = '20180112123301'; // { on_keyspace: report, on_table: lesson_use_by_year_bucketed_by_student_count }
DELETE FROM changelog.db where id = '20180112131626'; // { on_keyspace: report, on_table: all_time_instructors_deployed_student }
DELETE FROM changelog.db where id = '20180112131642'; // { on_keyspace: report, on_table: all_time_instructors_deployed_student_five_plus }
DELETE FROM changelog.db where id = '20180112133552'; // { on_keyspace: report, on_table: all_time_instructors_deployed_lesson }
DELETE FROM changelog.db where id = '20180112133606'; // { on_keyspace: report, on_table: all_time_instructors_deployed_lesson_five_plus }
DELETE FROM changelog.db where id = '20180112143852'; // { on_keyspace: report, on_table: fy_instructors_deployed_lesson }
DELETE FROM changelog.db where id = '20180112143906'; // { on_keyspace: report, on_table: fy_instructors_deployed_lesson_five_plus }
DELETE FROM changelog.db where id = '20180112145515'; // { on_keyspace: report, on_table: fy_instructors_deployed_to_student }
DELETE FROM changelog.db where id = '20180112145529'; // { on_keyspace: report, on_table: fy_instructors_deployed_to_student_five_plus }
DELETE FROM changelog.db where id = '20180112160531'; // { on_keyspace: report, on_table: hy_instructors_deployed_lesson }
DELETE FROM changelog.db where id = '20180112160546'; // { on_keyspace: report, on_table: hy_instructors_deployed_lesson_five_plus }
DELETE FROM changelog.db where id = '20180112162338'; // { on_keyspace: report, on_table: hy_instructors_deployed_to_student }
DELETE FROM changelog.db where id = '20180112162353'; // { on_keyspace: report, on_table: hy_instructors_deployed_to_student_five_plus }
DELETE FROM changelog.db where id = '20180112165350'; // { on_keyspace: report, on_table: weekly_instructors_deployed_lesson }
DELETE FROM changelog.db where id = '20180112165401'; // { on_keyspace: report, on_table: weekly_instructors_deployed_lesson_five_plus }
DELETE FROM changelog.db where id = '20180112170530'; // { on_keyspace: report, on_table: weekly_instructors_deployed_student }
DELETE FROM changelog.db where id = '20180112170541'; // { on_keyspace: report, on_table: weekly_instructors_deployed_student_five_plus }
DELETE FROM changelog.db where id = '20180112173704'; // { on_keyspace: report, on_table: monthly_cumulative_deployed_lesson }
DELETE FROM changelog.db where id = '20180112173715'; // { on_keyspace: report, on_table: monthly_cumulative_deployed_lesson_five_plus }
DELETE FROM changelog.db where id = '20180112173813'; // { on_keyspace: report, on_table: monthly_cumulative_unique_student_by_domain }
