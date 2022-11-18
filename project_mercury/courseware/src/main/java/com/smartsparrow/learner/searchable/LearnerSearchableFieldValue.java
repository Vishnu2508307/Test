package com.smartsparrow.learner.searchable;

import java.util.Objects;

public class LearnerSearchableFieldValue {

    private final String summary;
    private final String body;
    private final String source;
    private final String preview;
    private final String tag;
    private final boolean isEmpty;

    public LearnerSearchableFieldValue(String summary, String body, String source, String preview, String tag) {
        this.summary = summary;
        this.body = body;
        this.source = source;
        this.preview = preview;
        this.tag = tag;

        this.isEmpty = summary.equals("")
                && body.equals("")
                && source.equals("")
                && preview.equals("")
                && tag.equals("");
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public String getSource() {
        return source;
    }

    public String getPreview() {
        return preview;
    }

    public String getTag() {
        return tag;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerSearchableFieldValue that = (LearnerSearchableFieldValue) o;
        return isEmpty == that.isEmpty &&
                Objects.equals(summary, that.summary) &&
                Objects.equals(body, that.body) &&
                Objects.equals(source, that.source) &&
                Objects.equals(preview, that.preview) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(summary, body, source, preview, tag, isEmpty);
    }

    @Override
    public String toString() {
        return "LearnerSearchableFieldValue{" +
                "summary='" + summary + '\'' +
                ", body='" + body + '\'' +
                ", source='" + source + '\'' +
                ", preview='" + preview + '\'' +
                ", tag='" + tag + '\'' +
                ", isEmpty=" + isEmpty +
                '}';
    }
}
