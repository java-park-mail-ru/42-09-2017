package ru.mail.park.mechanics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.mechanics.listeners.SensorListener;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.mechanics.objects.body.*;
import ru.mail.park.mechanics.objects.joint.GJoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldParser implements Runnable {

    private boolean calculation = true;
    private World world;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldParser.class);
    private Map<Long, Body> gameBodies;
    private Map<Long, Body> dynamicBodies;
    private Map<Long, Map<Long, BodyFrame>> diffsPerFrame;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final long TIMEOUT = 50000000000L;
    private static final float GRAVITY = -10f;
    private static final float DELTA = 1 / 60f;
    private static final int VEL_ITER = 10;
    private static final int POS_ITER = 10;
    private static final int SECOND = 1000000000;
    private static final int MICRO_SECOND = 1000000;
    private static final int FPS = 60;

    public WorldParser() {
        this.world = new World(new Vec2(0f, GRAVITY));
        this.gameBodies = new ConcurrentHashMap<>();
        this.dynamicBodies = new ConcurrentHashMap<>();
        this.diffsPerFrame = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        long beforeTime = startTime;
        long afterTime;
        long sleepTime;
        long frameNumber = 0;


        LOGGER.warn("Start running");
        while (calculation) {
            if ((beforeTime - startTime) > TIMEOUT) {
                calculation = false;
                LOGGER.error("Running timeout");
            }

            frameNumber++;
            LOGGER.warn("FRAME #" + String.valueOf(frameNumber));
            for (Map.Entry<Long, Body> bodyEntry : dynamicBodies.entrySet()) {
                long bodyId = bodyEntry.getKey();
                Body body = bodyEntry.getValue();
                Map<Long, BodyFrame> bodyDiffMap = diffsPerFrame.get(bodyId);
                bodyDiffMap.computeIfAbsent(frameNumber, ignored -> {
                    BodyFrame bodyFrame = new BodyFrame();
                    bodyFrame.setPosition(new Vec2(body.getPosition()));
                    bodyFrame.setVelocity(new Vec2(body.getLinearVelocity()));
                    bodyFrame.setAngle(body.getAngle());
                    try {
                        LOGGER.error(mapper.writeValueAsString(bodyFrame));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return bodyFrame;
                });
            }
            world.step(DELTA, VEL_ITER, POS_ITER);
            afterTime = System.nanoTime();

            sleepTime = (SECOND / FPS - (afterTime - beforeTime)) / MICRO_SECOND;
            if (sleepTime < 0) {
                sleepTime = 0;
            }
            try {
                LOGGER.info(String.valueOf(sleepTime));
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                LOGGER.error("Sleep interrupted");
            }

            beforeTime = System.nanoTime();
        }
    }

    public void initWorld(List<GBody> bodies, List<GJoint> joints) {
        LOGGER.info("World initialization started");
        for (GBody gbody : bodies) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.values()[gbody.getData().getType()];
            Vec2 position = gbody.getData().getPosition();
            bodyDef.position = new Vec2(position.x, -position.y);
            bodyDef.angle = -gbody.getData().getAngle();
            Body body = world.createBody(bodyDef);
            gameBodies.put(gbody.getId(), body);
            if (bodyDef.type == BodyType.DYNAMIC) {
                dynamicBodies.put(gbody.getId(), body);
                diffsPerFrame.put(gbody.getId(), new ConcurrentHashMap<>());
            }

            LOGGER.info("   Body created with options: "
                    + bodyDef.type.toString() + ", "
                    + bodyDef.position.toString() + ", "
                    + String.valueOf(bodyDef.angle));

            String kind = gbody.getKind();
            BodyData bodyData = gbody.getData();
            switch (kind) {
                case "rect":
                    rectCreator(body, bodyData);
                    LOGGER.info("   Rectangle created");
                    break;
                case "circle":
                    circleCreator(body, bodyData);
                    LOGGER.info("   Circle created");
                    break;
                case "bucket":
                    bucketCreator(body, bodyData);
                    LOGGER.info("   Bucket created");
                    break;
                default:
                    break;
            }

            for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
                LOGGER.info("   Fixture created with properties: "
                        + String.valueOf(fixture.m_isSensor) + ", "
                        + String.valueOf(fixture.m_density) + ", "
                        + String.valueOf(fixture.m_friction) + ", "
                        + String.valueOf(fixture.m_restitution) + ", "
                        + String.valueOf(fixture.m_filter.categoryBits)
                );
            }
        }
        world.setContactListener(new SensorListener(this));
        LOGGER.warn("All bodies created");
    }

    private void setPhysicalProperties(BodyOptions options, boolean simpleBody, FixtureDef... fixDefs) {
        float density = options.getDensity();
        float friction = options.getFriction();
        float restitution = options.getRestitution();
        for (FixtureDef fixDef : fixDefs) {
            fixDef.density = density;
            fixDef.friction = friction;
            fixDef.restitution = restitution;
            if (simpleBody) {
                fixDef.isSensor = options.isSensor();
                fixDef.filter.categoryBits = options.getKeyBodyID();
            }
        }
    }

    private void rectCreator(Body body, BodyData bodyData) {
        Vec2 size = bodyData.getSize();
        if (size == null) {
            throw new RuntimeException("Size is null");
        }
        FixtureDef fixDef = new FixtureDef();
        fixDef.shape = new PolygonShape();
        ((PolygonShape) fixDef.shape).setAsBox(size.x / 2, size.y / 2);

        setPhysicalProperties(bodyData.getOptions(), true, fixDef);
        body.createFixture(fixDef);
    }

    private void circleCreator(Body body, BodyData bodyData) {
        Float radius = bodyData.getRadius();
        if (radius == null) {
            throw new RuntimeException("Radius is null");
        }
        FixtureDef fixDef = new FixtureDef();
        fixDef.shape = new CircleShape();
        fixDef.shape.setRadius(radius);

        setPhysicalProperties(bodyData.getOptions(), true, fixDef);
        body.createFixture(fixDef);
    }

    private void bucketCreator(Body body, BodyData bodyData) {
        float wallWidth;
        float bottomLength;
        float height;
        ComplexBodyConfig config = bodyData.getConfig();
        try {
            wallWidth = config.getWallThickness();
            bottomLength = config.getBottomLength();
            height = config.getHeight();
        } catch (NullPointerException e) {
            throw new RuntimeException("Config is null");
        }

        int notKeyBodyBits = 0x0001;

        FixtureDef fixDefLeft = new FixtureDef();
        fixDefLeft.shape = new PolygonShape();
        ((PolygonShape) fixDefLeft.shape).setAsBox(wallWidth / 2, height / 2,
                new Vec2(-(bottomLength + wallWidth) / 2, 0), 0);
        fixDefLeft.filter.categoryBits = notKeyBodyBits;

        FixtureDef fixDefRight = new FixtureDef();
        fixDefRight.shape = new PolygonShape();
        ((PolygonShape) fixDefRight.shape).setAsBox(wallWidth / 2, height / 2,
                new Vec2((bottomLength + wallWidth) / 2, 0), 0);
        fixDefRight.filter.categoryBits = notKeyBodyBits;

        FixtureDef fixDefDown = new FixtureDef();
        fixDefDown.shape = new PolygonShape();
        ((PolygonShape) fixDefDown.shape).setAsBox(bottomLength / 2, wallWidth / 2,
                new Vec2(0, -(height - wallWidth) / 2), 0);
        fixDefDown.filter.categoryBits = notKeyBodyBits;

        FixtureDef fixDefSensor = new FixtureDef();
        fixDefSensor.shape = new PolygonShape();
        ((PolygonShape) fixDefSensor.shape).setAsBox(bottomLength / 2, (height - wallWidth) / 2,
                new Vec2(0f, wallWidth / 2), 0);
        fixDefSensor.isSensor = bodyData.getOptions().isSensor();
        fixDefSensor.filter.categoryBits = bodyData.getOptions().getKeyBodyID();

        setPhysicalProperties(bodyData.getOptions(), false,
                fixDefLeft, fixDefRight, fixDefDown, fixDefSensor);
        body.createFixture(fixDefLeft);
        body.createFixture(fixDefRight);
        body.createFixture(fixDefDown);
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

    public Map<Long, Map<Long, BodyFrame>> getDiffsPerFrame() {
        return diffsPerFrame;
    }

    public void setDiffsPerFrame(Map<Long, Map<Long, BodyFrame>> diffsPerFrame) {
        this.diffsPerFrame = diffsPerFrame;
    }
}
