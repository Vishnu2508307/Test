package com.smartsparrow.math.event;

import java.util.Objects;

public class MathAssetEventMessage {
    private String mathML;

    private Integer height;
    private Integer width;
    private String content;
    private Integer baseline;
    private String format;
    private String alt;
    private String role;

    public MathAssetEventMessage(String mml) {
        this.mathML = mml;
    }

    public String getMathML() {
        return mathML;
    }

    public void setMathML(final String mathML) {
        this.mathML = mathML;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(final Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(final Integer width) {
        this.width = width;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Integer getBaseline() {
        return baseline;
    }

    public void setBaseline(final Integer baseline) {
        this.baseline = baseline;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(final String alt) {
        this.alt = alt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetEventMessage that = (MathAssetEventMessage) o;
        return Objects.equals(mathML, that.mathML) &&
                Objects.equals(height, that.height) &&
                Objects.equals(width, that.width) &&
                Objects.equals(content, that.content) &&
                Objects.equals(baseline, that.baseline) &&
                Objects.equals(format, that.format) &&
                Objects.equals(alt, that.alt) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mathML, height, width, content, baseline, format, alt, role);
    }

    @Override
    public String toString() {
        return "MathAssetEventMessage{" +
                "mathML='" + mathML + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", content='" + content + '\'' +
                ", baseline=" + baseline +
                ", format='" + format + '\'' +
                ", alt='" + alt + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
