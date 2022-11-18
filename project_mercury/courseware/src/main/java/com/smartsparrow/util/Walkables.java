package com.smartsparrow.util;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.ArrayList;
import java.util.List;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.data.WalkablePathwayChildren;

public final class Walkables {

    /**
     * Helper method to convert {@link WalkablePathwayChildren} to the list of {@link WalkableChild}
     * @param children the children object
     * @return list of walkable child
     */
    public static List<WalkableChild> toList(WalkablePathwayChildren children) {
        affirmArgument(children != null, "children can not be null");
        affirmArgument(children.getWalkableIds() != null && children.getWalkableTypes() != null,
                "children ids and types can not be empty");

        List<WalkableChild> list = new ArrayList<>(children.getWalkableIds().size());
        children.getWalkableIds().forEach(id -> list.add(
                new WalkableChild()
                        .setElementId(id)
                        .setElementType(CoursewareElementType.valueOf(children.getWalkableTypes().get(id)))));
        return list;
    }
}
