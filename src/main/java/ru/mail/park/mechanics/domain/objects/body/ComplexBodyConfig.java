package ru.mail.park.mechanics.domain.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ComplexBodyConfig {
    private Float bottomLength;
    private Float wallThickness;
    private Float height;

    @JsonCreator
    public ComplexBodyConfig() {

    }

    public Float getBottomLength() {
        return bottomLength;
    }

    @SuppressWarnings("unused")
    public void setBottomLength(Float bottomLength) {
        this.bottomLength = bottomLength;
    }

    public Float getWallThickness() {
        return wallThickness;
    }

    @SuppressWarnings("unused")
    public void setWallThickness(Float wallThickness) {
        this.wallThickness = wallThickness;
    }

    public Float getHeight() {
        return height;
    }

    @SuppressWarnings("unused")
    public void setHeight(Float height) {
        this.height = height;
    }
}
