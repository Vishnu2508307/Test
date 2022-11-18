package com.smartsparrow.courseware.pathway;

import java.util.UUID;

public interface Pathway {

    UUID getId();

    PathwayType getType();

    PreloadPathway getPreloadPathway();

}
