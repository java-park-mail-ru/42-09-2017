package ru.mail.park.mechanics.objects.fixture;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.jbox2d.common.Vec2;

import java.util.List;

public class GFixture {
    private String type;
    private Vec2 position;
    private float width;
    private float height;
    private float radius;
    private List<Vec2> vertices;
    private float density;
    private float friction;
    private float restitution;
    private float angle;

    @JsonCreator
    public GFixture() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Vec2 getPosition() {
        return position;
    }

    public void setPosition(Vec2 position) {
        this.position = position;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public List<Vec2> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vec2> vertices) {
        this.vertices = vertices;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}
