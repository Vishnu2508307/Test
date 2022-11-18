package mercury.helpers.learner;

public class LearnerCohortHelper {

    public static String fetchCohort(String cohortId) {
        return "{ " +
                "learn { " +
                    "cohort(cohortId: \"" + cohortId + "\") { " +
                        "id " +
                        "name " +
                    "} " +
                "} " +
            "} ";
    }
}
