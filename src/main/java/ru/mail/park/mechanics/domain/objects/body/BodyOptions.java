package ru.mail.park.mechanics.domain.objects.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.info.constants.MessageConstants;

import javax.validation.constraints.NotNull;

public class BodyOptions {
    private boolean sensor;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float density;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float friction;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Float restitution;
    @NotNull(message = MessageConstants.REQUIRED_FIELD_EMPTY)
    private Integer keyBodyID;

    @JsonCreator
    public BodyOptions() {

    }

    public boolean isSensor() {
        return sensor;
    }

    @SuppressWarnings("unused")
    public void setSensor(boolean sensor) {
        this.sensor = sensor;
    }

    public Float getDensity() {
        return density;
    }

    @SuppressWarnings("unused")
    public void setDensity(Float density) {
        this.density = density;
    }

    public Float getFriction() {
        return friction;
    }

    @SuppressWarnings("unused")
    public void setFriction(Float friction) {
        this.friction = friction;
    }

    public Float getRestitution() {
        return restitution;
    }

    @SuppressWarnings("unused")
    public void setRestitution(Float restitution) {
        this.restitution = restitution;
    }

    public Integer getKeyBodyID() {
        return keyBodyID;
    }

    @SuppressWarnings("unused")
    public void setKeyBodyID(Integer keyBodyID) {
        this.keyBodyID = keyBodyID;
    }
}
