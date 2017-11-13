package ru.mail.park.mechanics;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.mechanics.listeners.SensorListener;
import ru.mail.park.mechanics.objects.body.BodyDiff;
import ru.mail.park.mechanics.objects.joint.GJoint;
import ru.mail.park.mechanics.objects.body.BodyOption;
import ru.mail.park.mechanics.objects.body.ComplexBodyConfig;
import ru.mail.park.mechanics.objects.body.GBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldParser implements Runnable {

    private boolean calculation = true;
    private World world = new World(new Vec2(0f, -10f));
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldParser.class);
    private Map<Long, Body> gameBodies = new ConcurrentHashMap<>();
    private Map<Long, Body> dynamicBodies = new ConcurrentHashMap<>();
    private Map<Long, ConcurrentHashMap<Long, BodyDiff>> diffsPerFrame = new ConcurrentHashMap<>();

    @Override
    public void run() {
        long startTime = System.nanoTime();
        long beforeTime = startTime;
        long afterTime, sleepTime;
        long frameNumber = 0;
        LOGGER.warn("Start running");
        while (calculation) {

            if ((beforeTime - startTime) > 30000000000L) {
                calculation = false;
                LOGGER.error("Running timeout");
            }

            LOGGER.warn("FRAME #" + String.valueOf(frameNumber));

            world.step(1 / 60f, 10, 10);

            frameNumber++;
            for (Map.Entry<Long, Body> bodyEntry : dynamicBodies.entrySet()) {
                long bodyId = bodyEntry.getKey();
                Body body = bodyEntry.getValue();
                Map<Long, BodyDiff> bodyDiffMap = diffsPerFrame.get(bodyId);
                BodyDiff bodyDiff = new BodyDiff();
                bodyDiff.setId(bodyId);
                bodyDiff.setPosition(body.getPosition());
                bodyDiff.setAngle(body.getAngle());
                bodyDiffMap.put(frameNumber, bodyDiff);
            }
            afterTime = System.nanoTime();

            sleepTime = (1000000000 / 60 - (afterTime - beforeTime)) / 1000000;
            try {
                Thread.sleep(sleepTime);
                LOGGER.info(String.valueOf(sleepTime));
            } catch (InterruptedException e) {
                LOGGER.error("Sleep interrupted");
            }

            beforeTime = System.nanoTime();
        }
        LOGGER.warn("Running completed");
    }

    public void initWorld(List<GBody> bodies, List<GJoint> joints) {
        LOGGER.info("World initialization started");
        gameBodies = new ConcurrentHashMap<>();
        dynamicBodies = new ConcurrentHashMap<>();
        diffsPerFrame = new ConcurrentHashMap<>();

        for (GBody gBody : bodies) {

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.values()[gBody.getType()];
            Vec2 position = gBody.getBody().getData().getPosition();
            bodyDef.position = new Vec2(position.x, -position.y);
            bodyDef.angle = -gBody.getBody().getData().getAngle();
            Body body = world.createBody(bodyDef);
            gameBodies.put(gBody.getId(), body);
            if (bodyDef.type == BodyType.DYNAMIC) {
                dynamicBodies.put(gBody.getId(), body);
                diffsPerFrame.put(gBody.getId(), new ConcurrentHashMap<>());
            }

            LOGGER.info("   Body created with options: " +
                    bodyDef.type.toString() + ", " +
                    bodyDef.position.toString() + ", " +
                    String.valueOf(bodyDef.angle));

            String type = gBody.getBody().getType();
            switch (type) {
                case "rect":
                    rectCreator(body, gBody.getBody().getData().getSize());
                    LOGGER.info("   Rectangle created");
                    break;
                case "circle":
                    circleCreator(body, gBody.getBody().getData().getRadius());
                    LOGGER.info("   Circle created");
                    break;
                case "bucket":
                    bucketCreator(body, gBody.getBody().getData().getConfig(), gBody);
                    LOGGER.info("   Bucket created");
                    break;
            }

            BodyOption option = gBody.getBody().getData().getOption();
            float density = option.getDensity();
            float friction = option.getFriction();
            float restitution = option.getRestitution();
            boolean sensor = option.isSensor();
            int categoryBits;
            if (gBody.isKeyBody() && gBody.getKeyBodyId() != null) {
                categoryBits = gBody.getKeyBodyId();
            } else {
                categoryBits = 0x0001;
            }
            for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
                fixture.setDensity(density);
                fixture.setFriction(friction);
                fixture.setRestitution(restitution);
                if (!type.equals("bucket")) {
                    fixture.setSensor(sensor);
                    fixture.getFilterData().categoryBits = categoryBits;
                } else {
                    if (fixture.isSensor()) {
                        fixture.getFilterData().categoryBits = categoryBits;
                    } else {
                        fixture.getFilterData().categoryBits = 0x0001;
                    }
                }
                LOGGER.info("   Fixture created with properties: " +
                        String.valueOf(fixture.m_isSensor) + ", " +
                        String.valueOf(fixture.m_density) + ", " +
                        String.valueOf(fixture.m_friction) + ", " +
                        String.valueOf(fixture.m_restitution) + ", " +
                        String.valueOf(fixture.m_filter.categoryBits)
                );
            }
            world.setContactListener(new SensorListener(this));
        }
        LOGGER.warn("All bodies created");
    }

    public void rectCreator(Body body, Vec2 size) {
        if (size == null) {
            throw new RuntimeException("Size is null");
        }
        FixtureDef fixDef = new FixtureDef();
        fixDef.shape = new PolygonShape();
        ((PolygonShape) fixDef.shape).setAsBox(size.x / 2, size.y / 2);
        body.createFixture(fixDef);
    }

    public void circleCreator(Body body, Float radius) {
        if (radius == null) {
            throw new RuntimeException("Radius is null");
        }
        FixtureDef fixDef = new FixtureDef();
        fixDef.shape = new CircleShape();
        fixDef.shape.setRadius(radius);
        body.createFixture(fixDef);
    }

    public void bucketCreator(Body body, ComplexBodyConfig config, GBody gBody) {
        float wallWidth;
        float bottomLength;
        float height;
        try {
            wallWidth = config.getWallThickness();
            bottomLength = config.getBottomLength();
            height = config.getHeight();
        } catch (NullPointerException e) {
            throw new RuntimeException("Config is null");
        }

        FixtureDef fixDefLeft = new FixtureDef();
        fixDefLeft.shape = new PolygonShape();
        ((PolygonShape) fixDefLeft.shape).setAsBox(wallWidth / 2, height / 2,
                new Vec2(-(bottomLength + wallWidth) / 2, 0), 0);
        body.createFixture(fixDefLeft);

        FixtureDef fixDefRight = new FixtureDef();
        fixDefRight.shape = new PolygonShape();
        ((PolygonShape) fixDefRight.shape).setAsBox(wallWidth / 2, height / 2,
                new Vec2((bottomLength + wallWidth) / 2, 0), 0);
        body.createFixture(fixDefRight);

        FixtureDef fixDefDown = new FixtureDef();
        fixDefDown.shape = new PolygonShape();
        ((PolygonShape) fixDefDown.shape).setAsBox(bottomLength / 2, wallWidth / 2,
                new Vec2(0, -(height - wallWidth) / 2), 0);
        body.createFixture(fixDefDown);

        FixtureDef fixDefSensor = new FixtureDef();
        fixDefSensor.shape = new PolygonShape();
        ((PolygonShape) fixDefSensor.shape).setAsBox(bottomLength / 2, (height - wallWidth) / 2,
                new Vec2(0f, wallWidth / 2), 0);
        fixDefSensor.isSensor = gBody.getBody().getData().getOption().isSensor();
        body.createFixture(fixDefSensor);
    }

    public boolean isCalculation() {
        return calculation;
    }

    public void setCalculation(boolean calculation) {
        this.calculation = calculation;
    }

    public Map<Long, Body> getGameBodies() {
        return gameBodies;
    }

    public void setGameBodies(Map<Long, Body> gameBodies) {
        this.gameBodies = gameBodies;
    }

    public Map<Long, Body> getDynamicBodies() {
        return dynamicBodies;
    }

    public void setDynamicBodies(Map<Long, Body> dynamicBodies) {
        this.dynamicBodies = dynamicBodies;
    }

    public Map<Long, ConcurrentHashMap<Long, BodyDiff>> getDiffsPerFrame() {
        return diffsPerFrame;
    }

    public void setDiffsPerFrame(Map<Long, ConcurrentHashMap<Long, BodyDiff>> diffsPerFrame) {
        this.diffsPerFrame = diffsPerFrame;
    }
}
