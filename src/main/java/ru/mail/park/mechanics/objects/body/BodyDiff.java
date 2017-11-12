package ru.mail.park.mechanics.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jbox2d.common.Vec2;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.constraints.NotNull;

public class BodyDiff {
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Long id;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @JsonIgnoreProperties({"valid"})
    private Vec2 position;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float angle;

    @JsonCreator
    public BodyDiff() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vec2 getPosition() {
        return position;
    }

    public void setPosition(Vec2 position) {
        this.position = position;
    }

    public Float getAngle() {
        return angle;
    }

    public void setAngle(Float angle) {
        this.angle = angle;
    }
}
