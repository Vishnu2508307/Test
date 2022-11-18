package com.smartsparrow.rtm.message.recv.math;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class MathAssetCreateMessage extends ReceivedMessage implements CoursewareElementMessage {
    private String mathML;
    private UUID elementId;
    private CoursewareElementType elementType;
    private String altText;

    public String getMathML() {
        return mathML;
    }

    public MathAssetCreateMessage setMathML(final String mathML) {
        this.mathML = mathML;
        return this;
    }

    public String getAltText() {
        return altText;
    }

    public MathAssetCreateMessage setAltText(final String altText) {
        this.altText = altText;
        return this;
    }

    @Override
    public UUID getElementId() {
        return elementId;
    }

    public MathAssetCreateMessage setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public MathAssetCreateMessage setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetCreateMessage that = (MathAssetCreateMessage) o;
        return Objects.equals(mathML, that.mathML) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(altText, that.altText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mathML, elementId, elementType, altText);
    }

    @Override
    public String toString() {
        return "MathAssetCreateMessage{" +
                "mathML='" + mathML + '\'' +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", altText='" + altText + '\'' +
                '}';
    }
}
