package ru.mail.park.mechanics;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.exceptions.FramesOutOfBoundException;
import ru.mail.park.mechanics.domain.objects.BodyFrame;
import ru.mail.park.mechanics.domain.objects.body.BodyData;
import ru.mail.park.mechanics.domain.objects.body.BodyOptions;
import ru.mail.park.mechanics.domain.objects.body.ComplexBodyConfig;
import ru.mail.park.mechanics.domain.objects.body.GBody;
import ru.mail.park.mechanics.domain.objects.joint.GJoint;
import ru.mail.park.mechanics.listeners.SensorListener;
import ru.mail.park.websocket.message.from.SnapMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.mail.park.info.constants.Constants.*;

@Service
public class WorldRunnerService {
    private final Map<GameSession, WorldRunner> worldRunnerMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(SIMULATION_THREAD_POOL_SIZE);

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldRunnerService.class);

    public WorldRunnerService() {

    }

    public WorldRunner getWorldRunnerFor(GameSession gameSession) {
        return worldRunnerMap.get(gameSession);
    }

    public void removeWorldRunnerFor(GameSession gameSession) {
        worldRunnerMap.remove(gameSession);
    }

    public void initAndRun(GameSession session) {
        executorService.submit(() -> {
            LOGGER.warn("Starting simulation in new thread");
            final BoardRequest.Data board = session.getBoard();
            final Map<Long, GBody> bodiesMap = new HashMap<>();
            board.getBodies().forEach(body -> bodiesMap.put(body.getId(), body));
            final Map<Id<User>, List<BodyFrame>> initSnapsMap = session.getInitSnapsMap();
            for (Map.Entry<Id<User>, List<BodyFrame>> initSnap : initSnapsMap.entrySet()) {
                initSnap.getValue().forEach(bodyFrame -> {
                    final BodyData bodyData = bodiesMap.get(bodyFrame.getId()).getData();
                    bodyData.setPosition(bodyFrame.getPosition());
                    bodyData.setAngle(bodyFrame.getAngle());
                });
            }
            final WorldRunner worldRunner = initWorld(session, board);
            worldRunnerMap.put(session, worldRunner);
            String result = worldRunner.runSimulation();
            session.setResult(result);
            session.setState(GameState.SIMULATED);
        });
    }

    public boolean checkSnap(GameSession session, SnapMessage snap) throws NullPointerException {
        final WorldRunner worldRunner = worldRunnerMap.get(session);
        LOGGER.info("Got changes");
        final long frameNumber = snap.getFrame();
        final long serverFrames = worldRunner.getFrames();
        LOGGER.info("   client frame: " + frameNumber);
        if (frameNumber > serverFrames) {
            if (frameNumber - serverFrames > MAX_FRAMES_DELTA) {
                throw new FramesOutOfBoundException();
            }
            throw new NullPointerException();
        }
        final List<BodyFrame> bodyFrames = snap.getBodies();
        boolean cheat = false;
        for (BodyFrame bodyFrame : bodyFrames) {
            final Map<Long, BodyFrame> serverDiffs = worldRunner.getDiffsPerFrame().get(bodyFrame.getId());
            final BodyFrame serverFrame = serverDiffs.get(frameNumber);
            final Vec2 serverPos = new Vec2(serverFrame.getPosition().x, -serverFrame.getPosition().y);
            final Vec2 serverLinVel = new Vec2(serverFrame.getLinVelocity().x, -serverFrame.getLinVelocity().y);
            final float serverAngVel = -serverFrame.getAngVelocity();
            final float serverAngle = -serverFrame.getAngle();
            final Vec2 posDiff = serverPos.sub(bodyFrame.getPosition()).abs();
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

    public WorldRunner initWorld(GameSession gameSession, BoardRequest.Data board) {
        LOGGER.info("World initialization started");
        final World world = new World(new Vec2(GRAVITY_X, GRAVITY_Y));
        final Map<Long, Body> gameBodies = new ConcurrentHashMap<>();
        final Map<Long, Body> dynamicBodies = new ConcurrentHashMap<>();
        final Map<Long, Map<Long, BodyFrame>> diffsPerFrame = new ConcurrentHashMap<>();

        final List<GBody> bodies = board.getBodies();
        final List<GJoint> joints = board.getJoints();
        for (GBody gbody : bodies) {
            final BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.values()[gbody.getData().getType()];
            final Vec2 position = gbody.getData().getPosition();
            bodyDef.position = new Vec2(position.x, -position.y);
            bodyDef.angle = -gbody.getData().getAngle();
            final Body body = world.createBody(bodyDef);
            gameBodies.put(gbody.getId(), body);
            if (bodyDef.type == BodyType.DYNAMIC) {
                diffsPerFrame.put(gbody.getId(), new ConcurrentHashMap<>());
                dynamicBodies.put(gbody.getId(), body);
            }

            LOGGER.info("   Body created with options: "
                    + bodyDef.type.toString() + ", "
                    + bodyDef.position.toString() + ", "
                    + String.valueOf(bodyDef.angle));

            final String kind = gbody.getKind();
            final BodyData bodyData = gbody.getData();
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
        final WorldRunner worldRunner = new WorldRunner(world, gameBodies, dynamicBodies, diffsPerFrame);
        world.setContactListener(new SensorListener(worldRunner));
        world.setContinuousPhysics(false);
        LOGGER.warn("All bodies created");
        return worldRunner;
    }

    private void setPhysicalProperties(BodyOptions options, boolean simpleBody, FixtureDef... fixDefs) {
        final float density = options.getDensity();
        final float friction = options.getFriction();
        final float restitution = options.getRestitution();
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
        final Vec2 size = bodyData.getSize();
        if (size == null) {
            throw new RuntimeException("Size is null");
        }
        final FixtureDef fixDef = new FixtureDef();
        fixDef.shape = new PolygonShape();
        ((PolygonShape) fixDef.shape).setAsBox(size.x / 2, size.y / 2);

        setPhysicalProperties(bodyData.getOptions(), true, fixDef);
        body.createFixture(fixDef);
    }

    private void circleCreator(Body body, BodyData bodyData) {
        final Float radius = bodyData.getRadius();
        if (radius == null) {
            throw new RuntimeException("Radius is null");
        }
        final FixtureDef fixDef = new FixtureDef();
        fixDef.shape = new CircleShape();
        fixDef.shape.setRadius(radius);

        setPhysicalProperties(bodyData.getOptions(), true, fixDef);
        body.createFixture(fixDef);
    }

    private void bucketCreator(Body body, BodyData bodyData) {
        final float wallWidth;
        final float bottomLength;
        final float height;
        final ComplexBodyConfig config = bodyData.getConfig();
        try {
            wallWidth = config.getWallThickness();
            bottomLength = config.getBottomLength();
            height = config.getHeight();
        } catch (NullPointerException e) {
            throw new RuntimeException("Config is null", e);
        }

        final int notKeyBodyBits = 0x0001;

        final FixtureDef fixDefLeft = new FixtureDef();
        fixDefLeft.shape = new PolygonShape();
        ((PolygonShape) fixDefLeft.shape).setAsBox(wallWidth / 2, height / 2,
                new Vec2(-(bottomLength + wallWidth) / 2, 0), 0);
        fixDefLeft.filter.categoryBits = notKeyBodyBits;

        final FixtureDef fixDefRight = new FixtureDef();
        fixDefRight.shape = new PolygonShape();
        ((PolygonShape) fixDefRight.shape).setAsBox(wallWidth / 2, height / 2,
                new Vec2((bottomLength + wallWidth) / 2, 0), 0);
        fixDefRight.filter.categoryBits = notKeyBodyBits;

        final FixtureDef fixDefDown = new FixtureDef();
        fixDefDown.shape = new PolygonShape();
        ((PolygonShape) fixDefDown.shape).setAsBox(bottomLength / 2, wallWidth / 2,
                new Vec2(0, -(height - wallWidth) / 2), 0);
        fixDefDown.filter.categoryBits = notKeyBodyBits;

        final FixtureDef fixDefSensor = new FixtureDef();
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
