-- element should associate all the variants, so dropping this column
ALTER TABLE courseware.theme_by_element
  DROP variant_id;
