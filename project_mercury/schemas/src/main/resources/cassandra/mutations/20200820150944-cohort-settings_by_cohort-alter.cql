-- Removing legacy payment requirement BRNT-409
ALTER TABLE cohort.settings_by_cohort
  DROP payment_required
;
