package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jbox2d.common.Vec2;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class BodyData {
    @JsonIgnoreProperties({ "valid" })
    private Vec2 size;
    private Float radius;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float angle;
    private ComplexBodyConfig config;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @Valid
    private BodyOptions options;
    @JsonIgnoreProperties({ "valid" })
    private Vec2 position;
    @Min(value = 0, message = MessageConstants.TYPE_OUT_OF_BOUNDS)
    @Max(value = 2, message = MessageConstants.TYPE_OUT_OF_BOUNDS)
    private Integer type;

    @JsonCreator
    public BodyData() {

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

    public BodyOptions getOptions() {
        return options;
    }

    public void setOptions(BodyOptions options) {
        this.options = options;
    }

    public Vec2 getPosition() {
        return position;
    }

    public void setPosition(Vec2 position) {
        this.position = position;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
