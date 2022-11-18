ALTER TABLE cohort.settings_by_cohort
  ADD (payment_required boolean,
       payment_methods_allowed set<text>,
       cost_currency_code text,
       cost_amount_in_cents int);
