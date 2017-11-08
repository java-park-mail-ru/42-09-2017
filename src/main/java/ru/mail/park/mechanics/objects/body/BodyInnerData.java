package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jbox2d.common.Vec2;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BodyInnerData {
    @JsonIgnoreProperties({ "valid" })
    private Vec2 size;
    private Float radius;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float angle;
    private ComplexBodyConfig config;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @Valid
    private BodyOption option;
    @JsonIgnoreProperties({ "valid" })
    private Vec2 position;

    @JsonCreator
    public BodyInnerData() {

    }

    public Vec2 getSize() {
        return size;
    }

    public void setSize(Vec2 size) {
        this.size = size;
    }

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public Float getAngle() {
        return angle;
    }

    public void setAngle(Float angle) {
        this.angle = angle;
    }

    public ComplexBodyConfig getConfig() {
        return config;
    }

    public void setConfig(ComplexBodyConfig config) {
        this.config = config;
    }

    public BodyOption getOption() {
        return option;
    }

    public void setOption(BodyOption option) {
        this.option = option;
    }

    public Vec2 getPosition() {
        return position;
    }

    public void setPosition(Vec2 position) {
        this.position = position;
    }
}
