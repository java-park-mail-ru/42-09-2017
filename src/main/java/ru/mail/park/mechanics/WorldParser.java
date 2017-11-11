//package ru.mail.park.mechanics;
//
//import org.jbox2d.collision.shapes.CircleShape;
//import org.jbox2d.collision.shapes.PolygonShape;
//import org.jbox2d.common.Vec2;
//import org.jbox2d.dynamics.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import ru.mail.park.mechanics.listeners.SensorListener;
//import ru.mail.park.mechanics.objects.joint.GJoint;
//import ru.mail.park.mechanics.objects.body.BodyOption;
//import ru.mail.park.mechanics.objects.body.ComplexBodyConfig;
//import ru.mail.park.mechanics.objects.body.GBody;
//
//import java.util.List;
//
//public class WorldParser {
//
//    public static boolean calculation = true;
//    private static World world = new World(new Vec2(0f, -10f));
//    private static final Logger LOGGER = LoggerFactory.getLogger(WorldParser.class);
//
//    public static void run() {
//        long start = System.nanoTime();
//        long current;
//        LOGGER.warn("Start running");
//        while (calculation) {
//            current = System.nanoTime();
//            if ((current - start) > 30_000_000_000L) {
//                calculation = false;
//                LOGGER.error("Running timeout");
//            }
//            world.step(1/60f, 10, 10);
//        }
//        LOGGER.warn("Running completed");
//    }
//
//    public static void initWorld(List<GBody> bodies, List<GJoint> joints) {
//        for (GBody gBody : bodies) {
//
//            BodyDef bodyDef = new BodyDef();
//            bodyDef.type = BodyType.values()[gBody.getType()];
//            bodyDef.position = gBody.getBody().getData().getPosition();
//            bodyDef.angle = gBody.getBody().getData().getAngle();
//            Body body = world.createBody(bodyDef);
//
//            LOGGER.info("Body created with options: " +
//                    bodyDef.type.toString() + ", " +
//                    bodyDef.position.toString() + ", " +
//                    String.valueOf(bodyDef.angle));
//
//            String type = gBody.getBody().getType();
//            switch (type) {
//                case "rect":
//                    RectCreator(body, gBody.getBody().getData().getSize());
//                    LOGGER.info("Rectangle created");
//                    break;
//                case "circle":
//                    CircleCreator(body, gBody.getBody().getData().getRadius());
//                    LOGGER.info("Circle created");
//                    break;
//                case "bucket":
//                    BucketCreator(body, gBody.getBody().getData().getConfig(), gBody);
//                    LOGGER.info("Bucket created");
//                    break;
//            }
//
//            BodyOption option = gBody.getBody().getData().getOption();
//            float density = option.getDensity();
//            float friction = option.getFriction();
//            float restitution = option.getRestitution();
//            boolean sensor = option.isSensor();
//            int categoryBits;
//            if (gBody.isKeyBody() && gBody.getKeyBodyId() != null) {
//                categoryBits = gBody.getKeyBodyId();
//            } else {
//                categoryBits = 0x0001;
//            }
//            for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
//                fixture.setDensity(density);
//                fixture.setFriction(friction);
//                fixture.setRestitution(restitution);
//                if (!type.equals("bucket")) {
//                    fixture.setSensor(sensor);
//                    fixture.getFilterData().categoryBits = categoryBits;
//                } else {
//                    if (fixture.isSensor()) {
//                        fixture.getFilterData().categoryBits = categoryBits;
//                    } else {
//                        fixture.getFilterData().categoryBits = 0x0001;
//                    }
//                }
//                LOGGER.info("Fixture created with properties: " +
//                        String.valueOf(fixture.m_isSensor) + ", " +
//                        String.valueOf(fixture.m_density) + ", " +
//                        String.valueOf(fixture.m_friction) + ", " +
//                        String.valueOf(fixture.m_restitution) + ", " +
//                        String.valueOf(fixture.m_filter.categoryBits)
//                );
//            }
//            world.setContactListener(new SensorListener());
//        }
//        LOGGER.warn("All bodies created");
//    }
//
//    public static void RectCreator(Body body, Vec2 size) {
//        if (size == null) {
//            throw new RuntimeException("Size is null");
//        }
//        FixtureDef fixDef = new FixtureDef();
//        fixDef.shape = new PolygonShape();
//        ((PolygonShape) fixDef.shape).setAsBox(size.x, size.y);
//        body.createFixture(fixDef);
//    }
//
//    public static void CircleCreator(Body body, Float radius) {
//        if (radius == null) {
//            throw new RuntimeException("Radius is null");
//        }
//        FixtureDef fixDef = new FixtureDef();
//        fixDef.shape = new CircleShape();
//        fixDef.shape.setRadius(radius);
//        body.createFixture(fixDef);
//    }
//
//    public static void BucketCreator(Body body, ComplexBodyConfig config, GBody gBody) {
//        float wallWidth;
//        float bottomLength;
//        float height;
//        try {
//            wallWidth = config.getWallWidth();
//            bottomLength = config.getBottomLength();
//            height = config.getHeight();
//        } catch (NullPointerException e) {
//            throw new RuntimeException("Config is null");
//        }
//
//        FixtureDef fixDefLeft = new FixtureDef();
//        fixDefLeft.shape = new PolygonShape();
//        ((PolygonShape) fixDefLeft.shape).setAsBox(wallWidth / 2, height / 2,
//                new Vec2(-(bottomLength + wallWidth) / 2, 0), 0);
//        body.createFixture(fixDefLeft);
//
//        FixtureDef fixDefRight = new FixtureDef();
//        fixDefRight.shape = new PolygonShape();
//        ((PolygonShape) fixDefLeft.shape).setAsBox(wallWidth / 2, height / 2,
//                new Vec2((bottomLength + wallWidth) / 2, 0), 0);
//        body.createFixture(fixDefRight);
//
//        FixtureDef fixDefDown = new FixtureDef();
//        fixDefDown.shape = new PolygonShape();
//        ((PolygonShape) fixDefLeft.shape).setAsBox(bottomLength / 2, wallWidth / 2,
//                new Vec2((height - wallWidth) / 2, 0), 0);
//        body.createFixture(fixDefDown);
//
//        FixtureDef fixDefSensor = new FixtureDef();
//        fixDefSensor.shape = new PolygonShape();
//        ((PolygonShape) fixDefLeft.shape).setAsBox(bottomLength / 2, (height - wallWidth) / 2,
//                new Vec2(-wallWidth / 2, 0), 0);
//        fixDefSensor.isSensor = gBody.getBody().getData().getOption().isSensor();
//        body.createFixture(fixDefSensor);
//    }
//}
