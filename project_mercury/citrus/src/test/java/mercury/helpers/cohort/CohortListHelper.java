package mercury.helpers.cohort;

import com.fasterxml.jackson.core.JsonProcessingException;

import mercury.common.PayloadBuilder;

public class CohortListHelper {

    public static String listCohortRequest() throws JsonProcessingException {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "workspace.cohort.list");
        return pb.build();
    }

}
