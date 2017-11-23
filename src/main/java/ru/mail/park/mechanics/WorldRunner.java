package ru.mail.park.mechanics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.mechanics.objects.BodyFrame;

import java.util.Map;

import static ru.mail.park.info.constants.Constants.*;

public class WorldRunner implements Runnable {
    private World world;
    private boolean calculation = true;
    private long frames = 0L;
    private Map<Long, Body> gameBodies;
    private Map<Long, Body> dynamicBodies;
    private Map<Long, Map<Long, BodyFrame>> diffsPerFrame;


    private static final Logger LOGGER = LoggerFactory.getLogger(WorldRunner.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public WorldRunner(
            World world,
            Map<Long, Body> gameBodies,
            Map<Long, Body> dynamicBodies,
            Map<Long, Map<Long, BodyFrame>> diffsPerFrame
    ) {
        this.world = world;
        this.gameBodies = gameBodies;
        this.dynamicBodies = dynamicBodies;
        this.diffsPerFrame = diffsPerFrame;
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
            if (frameNumber / 60 > TIMEOUT) {
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
                    bodyFrame.setLinVelocity(new Vec2(body.getLinearVelocity()));
                    bodyFrame.setAngVelocity(body.getAngularVelocity());
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
//            afterTime = System.nanoTime();

//            sleepTime = (SECOND / FPS - (afterTime - beforeTime)) / MICRO_SECOND;
//            if (sleepTime < 0) {
//                sleepTime = 0;
//            }
//            try {
//                LOGGER.info(String.valueOf(sleepTime));
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e) {
//                LOGGER.error("Sleep interrupted");
//            }
//
//            beforeTime = System.nanoTime();
        }
        frames = frameNumber;
    }

    public boolean isCalculation() {
        return calculation;
    }

    public void setCalculation(boolean calculation) {
        this.calculation = calculation;
    }

    public long getFrames() {
        return frames;
    }

    public void setFrames(long frames) {
        this.frames = frames;
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
