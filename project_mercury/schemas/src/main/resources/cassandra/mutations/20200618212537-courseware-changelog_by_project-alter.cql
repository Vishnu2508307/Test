-- adding columns to store courseware element titles
ALTER TABLE courseware.changelog_by_project
    ADD (
    on_element_title text,
    on_parent_walkable_title text
    );
