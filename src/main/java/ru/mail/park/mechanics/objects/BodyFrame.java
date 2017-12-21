package ru.mail.park.mechanics.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jbox2d.common.Vec2;
import ru.mail.park.controllers.validators.groups.SimulationSnap;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.constraints.NotNull;

public class BodyFrame {
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Long id;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    @JsonIgnoreProperties({"valid"})
    private Vec2 position;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY, groups = {SimulationSnap.class})
    @JsonIgnoreProperties({"valid"})
    private Vec2 linVelocity;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY, groups = {SimulationSnap.class})
    @JsonIgnoreProperties({"valid"})
    private Float angVelocity;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float angle;

    @JsonCreator
    public BodyFrame() {

    }

    public BodyFrame(
        Long id,
        Vec2 position,
        Vec2 linVelocity,
        Float angle
    ) {
        this.id = id;
        this.position = position;
        this.linVelocity = linVelocity;
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

    public Vec2 getLinVelocity() {
        return linVelocity;
    }

    public void setLinVelocity(Vec2 linVelocity) {
        this.linVelocity = linVelocity;
    }

    public Float getAngVelocity() {
        return angVelocity;
    }

    public void setAngVelocity(Float angVelocity) {
        this.angVelocity = angVelocity;
    }

    public Float getAngle() {
        return angle;
    }

    public void setAngle(Float angle) {
        this.angle = angle;
    }
}
