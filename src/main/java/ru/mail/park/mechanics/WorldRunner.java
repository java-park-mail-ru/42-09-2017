package ru.mail.park.mechanics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.mechanics.domain.objects.BodyFrame;

import java.util.Map;

import static ru.mail.park.info.constants.Constants.*;

public class WorldRunner implements Runnable {
    private final World world;
    private boolean calculation = true;
    private long frames = 0L;
    private Map<Long, Body> gameBodies;
    private Map<Long, Body> dynamicBodies;
    private Map<Long, Map<Long, BodyFrame>> diffsPerFrame;

    private Map<Long, Long> playerScoreMap;

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


        LOGGER.warn("Start running");
        long frameNumber = 0;
        while (calculation) {
            if (frameNumber / FPS > TIMEOUT) {
                calculation = false;
                LOGGER.error("Running timeout");
            }
            frameNumber++;
            LOGGER.warn("FRAME #" + String.valueOf(frameNumber));
            for (Map.Entry<Long, Body> bodyEntry : dynamicBodies.entrySet()) {
                final long bodyId = bodyEntry.getKey();
                final Body body = bodyEntry.getValue();
                final Map<Long, BodyFrame> bodyDiffMap = diffsPerFrame.get(bodyId);
                bodyDiffMap.computeIfAbsent(frameNumber, ignored -> {
                    final BodyFrame bodyFrame = new BodyFrame();
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

    @SuppressWarnings("unused")
    public void setFrames(long frames) {
        this.frames = frames;
    }

    @SuppressWarnings("unused")
    public Map<Long, Body> getGameBodies() {
        return gameBodies;
    }

    @SuppressWarnings("unused")
    public void setGameBodies(Map<Long, Body> gameBodies) {
        this.gameBodies = gameBodies;
    }

    @SuppressWarnings("unused")
    public Map<Long, Body> getDynamicBodies() {
        return dynamicBodies;
    }

    @SuppressWarnings("unused")
    public void setDynamicBodies(Map<Long, Body> dynamicBodies) {
        this.dynamicBodies = dynamicBodies;
    }

    public Map<Long, Map<Long, BodyFrame>> getDiffsPerFrame() {
        return diffsPerFrame;
    }

    @SuppressWarnings("unused")
    public void setDiffsPerFrame(Map<Long, Map<Long, BodyFrame>> diffsPerFrame) {
        this.diffsPerFrame = diffsPerFrame;
    }

    public void setScore(Long playerId, Long score) {
        final Long oldScore = playerScoreMap.getOrDefault(playerId, 0L);
        playerScoreMap.put(playerId, oldScore + score);
    }
}
