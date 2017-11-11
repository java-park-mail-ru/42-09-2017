package ru.mail.park.mechanics.objects.joint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jbox2d.common.Vec2;

public class GJoint {
    private Long idA;
    private Long idB;
    @JsonIgnoreProperties({ "valid" })
    private Vec2 anchorA;
    @JsonIgnoreProperties({ "valid" })
    private Vec2 anchorB;
    private String type;

    @JsonCreator
    public GJoint() {

    }

    public Long getIdA() {
        return idA;
    }

    public void setIdA(Long idA) {
        this.idA = idA;
    }

    public Long getIdB() {
        return idB;
    }

    public void setIdB(Long idB) {
        this.idB = idB;
    }

    public Vec2 getAnchorA() {
        return anchorA;
    }

    public void setAnchorA(Vec2 anchorA) {
        this.anchorA = anchorA;
    }

    public Vec2 getAnchorB() {
        return anchorB;
    }

    public void setAnchorB(Vec2 anchorB) {
        this.anchorB = anchorB;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
