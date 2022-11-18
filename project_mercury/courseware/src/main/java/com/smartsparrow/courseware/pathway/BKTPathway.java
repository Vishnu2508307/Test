package com.smartsparrow.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class BKTPathway implements Pathway {

    /**
     * The number of screen that should be completed to mark this pathway as completed.
     * The student can complete this pathway with a P(Ln) value that does not meet the configured values. This mean that
     * the student has completed the value without learning the skill.
     * Any configured document item mastery will not be awarded in this case.
     */
    static final String EXIT_AFTER = "exitAfter";
    /**
     * The probability of Slip (mistake)
     */
    static final String P_S = "P_S";
    /**
     * The probability of Guess (random guess)
     */
    static final String P_G = "P_G";
    /**
     * The probability of Transition (learned)
     */
    static final String P_T = "P_T";
    /**
     * The configured initial P(Ln) value
     */
    static final String P_L0 = "P_L0";
    /**
     * The minimum P(Ln) value the student should achieve
     */
    static final String P_LN = "P_LN";
    /**
     * For how many consecutive screens the {@link BKTPathway#P_LN} value should be maintained to complete this pathway
     */
    static final String MAINTAIN_FOR = "maintainFor";

    /**
     * A list of configured document items that will be awarded to the student.
     * The configuration entry structure in the list is described by {@link BKTPathway.ConfiguredDocumentItem}.
     * Each configured document item will be awarded with a value that is equal to the P(Ln) result of the BKT
     * calculation
     */
    static final String COMPETENCY = "competency";

    private UUID id;
    private PathwayType type = PathwayType.ALGO_BKT;
    private PreloadPathway preloadPathway;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public PathwayType getType() {
        return type;
    }

    public BKTPathway setId(final UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public PreloadPathway getPreloadPathway() {
        return preloadPathway;
    }

    public BKTPathway setPreloadPathway(final PreloadPathway preloadPathway) {
        this.preloadPathway = preloadPathway;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BKTPathway that = (BKTPathway) o;
        return Objects.equals(id, that.id) &&
                type == that.type &&
                preloadPathway == that.preloadPathway;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, preloadPathway);
    }

    @Override
    public String toString() {
        return "BKTPathway{" +
                "id=" + id +
                ", type=" + type +
                ", preloadPathway=" + preloadPathway +
                '}';
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
    public static class ConfiguredDocumentItem {

        private UUID documentId;
        private UUID documentItemId;

        public UUID getDocumentId() {
            return documentId;
        }


        public UUID getDocumentItemId() {
            return documentItemId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfiguredDocumentItem that = (ConfiguredDocumentItem) o;
            return Objects.equals(documentId, that.documentId) &&
                    Objects.equals(documentItemId, that.documentItemId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(documentId, documentItemId);
        }

        @Override
        public String toString() {
            return "ConfiguredDocumentItem{" +
                    "documentId=" + documentId +
                    ", documentItemId=" + documentItemId +
                    '}';
        }
    }
}
