package com.smartsparrow.iam.data;

import com.smartsparrow.iam.service.Region;

class RegionKeyspace {

    /**
     * Map the data region to a keyspace.
     *
     * @param region the data region
     * @return the keyspace of the region
     */
    public static String map(Region region) {
        String s = String.format("iam_%s", region.name().toLowerCase());
        return s;
    }

    /**
     * Map the data region and table to a keyspace.table
     *
     * @param region the data region
     * @param table the table
     * @return the keyspace.table
     */
    public static String map(Region region, String table) {
        String s = String.format("iam_%s.%s", region.name().toLowerCase(), table);
        return s;
    }
}
