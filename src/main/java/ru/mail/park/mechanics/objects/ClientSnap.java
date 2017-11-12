package ru.mail.park.mechanics.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.mail.park.mechanics.objects.body.BodyDiff;

import javax.validation.Valid;
import java.util.List;

public class ClientSnap {
    private Long frame;
    @Valid
    private List<BodyDiff> bodies;

    @JsonCreator
    public ClientSnap() {

    }

    public Long getFrame() {
        return frame;
    }

    public void setFrame(Long frame) {
        this.frame = frame;
    }

    public List<BodyDiff> getBodies() {
        return bodies;
    }

    public void setBodies(List<BodyDiff> bodies) {
        this.bodies = bodies;
    }
}
