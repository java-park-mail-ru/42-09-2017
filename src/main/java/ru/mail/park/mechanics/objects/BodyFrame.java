package ru.mail.park.mechanics.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jbox2d.common.Vec2;
import ru.mail.park.controllers.validators.groups.InitSnap;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.constraints.NotNull;

public class BodyFrame {
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Long id;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @JsonIgnoreProperties({"valid"})
    private Vec2 position;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY, groups = {InitSnap.class})
    @JsonIgnoreProperties({"valid"})
    private Vec2 velocity;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float angle;

    @JsonCreator
    public BodyFrame() {

    }

    public BodyFrame(
        Long id,
        Vec2 position,
        Vec2 velocity,
        Float angle
    ) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.angle = angle;
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

    public Vec2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vec2 velocity) {
        this.velocity = velocity;
    }

    public Float getAngle() {
        return angle;
    }

    public void setAngle(Float angle) {
        this.angle = angle;
    }
}
