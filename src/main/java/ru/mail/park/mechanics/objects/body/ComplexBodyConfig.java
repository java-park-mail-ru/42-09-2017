package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ComplexBodyConfig {
    private Float bottomLength;
    private Float wallWidth;
    private Float height;

    @JsonCreator
    public ComplexBodyConfig() {

    }

    public Float getBottomLength() {
        return bottomLength;
    }

    public void setBottomLength(Float bottomLength) {
        this.bottomLength = bottomLength;
    }

    public Float getWallWidth() {
        return wallWidth;
    }

    public void setWallWidth(Float wallWidth) {
        this.wallWidth = wallWidth;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }
}
