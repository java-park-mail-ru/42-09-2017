package ru.mail.park.mechanics;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.exceptions.FramesOutOfBoundException;
import ru.mail.park.mechanics.listeners.SensorListener;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.mechanics.objects.body.BodyData;
import ru.mail.park.mechanics.objects.body.BodyOptions;
import ru.mail.park.mechanics.objects.body.ComplexBodyConfig;
import ru.mail.park.mechanics.objects.body.GBody;
import ru.mail.park.mechanics.objects.joint.GJoint;
import ru.mail.park.websocket.message.from.SnapMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.mail.park.info.constants.Constants.*;

@Service
public class WorldRunnerService {
    private Map<GameSession, WorldRunner> worldRunnerMap = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldRunnerService.class);

    public WorldRunnerService() {

    }

    public WorldRunner getWorldRunnerFor(GameSession gameSession) {
        return worldRunnerMap.get(gameSession);
    }

    public void removeWorldRunnerFor(GameSession gameSession) {
        worldRunnerMap.remove(gameSession);
    }

    // ToDo: 14.12.17 не задерживать WorldRunnerService симуляцией прямо здесь
    public void runSimulation(GameSession gameSession) {
        WorldRunner worldRunner = worldRunnerMap.get(gameSession);
        worldRunner.run();
        gameSession.setState(GameState.SIMULATED);
    }

    public boolean checkSnap(GameSession session, SnapMessage snap) throws NullPointerException {
        WorldRunner worldRunner = worldRunnerMap.get(session);
        LOGGER.info("Got changes");
        long frameNumber = snap.getFrame();
        long serverFrames = worldRunner.getFrames();
        LOGGER.info("   client frame: " + frameNumber);
        if (frameNumber > serverFrames) {
            if (frameNumber - serverFrames > MAX_FRAMES_DELTA) {
                throw new FramesOutOfBoundException();
            }
            throw new NullPointerException();
        }
        List<BodyFrame> bodyFrames = snap.getBodies();
        boolean cheat = false;
        for (BodyFrame bodyFrame : bodyFrames) {
            Map<Long, BodyFrame> serverDiffs = worldRunner.getDiffsPerFrame().get(bodyFrame.getId());
            BodyFrame serverFrame = serverDiffs.get(frameNumber);
            Vec2 serverPos = new Vec2(serverFrame.getPosition().x, -serverFrame.getPosition().y);
            Vec2 serverLinVel = new Vec2(serverFrame.getLinVelocity().x, -serverFrame.getLinVelocity().y);
            float serverAngVel = -serverFrame.getAngVelocity();
            float serverAngle = -serverFrame.getAngle();
            Vec2 posDiff = serverPos.sub(bodyFrame.getPosition());
            if (Math.max(posDiff.x, posDiff.y) > ALLOWED_POS_DELTA) {
                cheat = true;
                bodyFrame.setPosition(serverPos);
                bodyFrame.setLinVelocity(serverLinVel);
                bodyFrame.setAngVelocity(serverAngVel);
                bodyFrame.setAngle(serverAngle);
            }
        }
        return cheat;
    }

    public void initWorld(GameSession gameSession, BoardRequest.Data board) {
        LOGGER.info("World initialization started");
        World world = new World(new Vec2(GRAVITY_X, GRAVITY_Y));
        Map<Long, Body> gameBodies = new ConcurrentHashMap<>();
        Map<Long, Body> dynamicBodies = new ConcurrentHashMap<>();
        Map<Long, Map<Long, BodyFrame>> diffsPerFrame = new ConcurrentHashMap<>();

        List<GBody> bodies = board.getBodies();
        List<GJoint> joints = board.getJoints();
        for (GBody gbody : bodies) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.values()[gbody.getData().getType()];
            Vec2 position = gbody.getData().getPosition();
            bodyDef.position = new Vec2(position.x, -position.y);
            bodyDef.angle = -gbody.getData().getAngle();
            Body body = world.createBody(bodyDef);
            gameBodies.put(gbody.getId(), body);
            if (bodyDef.type == BodyType.DYNAMIC) {
                diffsPerFrame.put(gbody.getId(), new ConcurrentHashMap<>());
                dynamicBodies.put(gbody.getId(), body);
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
                    LOGGER.info("   Rectangle created with size: " + bodyData.getSize());
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

            body.setUserData(gbody);

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
        WorldRunner worldRunner = new WorldRunner(world, gameBodies, dynamicBodies, diffsPerFrame);
        world.setContactListener(new SensorListener(worldRunner));
        world.setContinuousPhysics(false);
        worldRunnerMap.put(gameSession, worldRunner);
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
}
