package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.mail.park.info.constants.Constants.*;

@Service
public class MechanicsExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MechanicsExecutor.class);
    private final GameMechanicsService gameMechanicsService;
    private final ScheduledExecutorService tickExecutor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    public MechanicsExecutor(
            GameMechanicsService gameMechanicsService
    ) {
        this.gameMechanicsService = gameMechanicsService;
    }

    @PostConstruct
    public void initAfterStartup() {
        Set<GameMechanics> mechanicsSet = gameMechanicsService.initMechanics(THREAD_POOL_SIZE);
        mechanicsSet.forEach(mechanics -> tickExecutor.scheduleAtFixedRate(mechanics::gameStep,
                0, TICK, TimeUnit.MILLISECONDS));
    }

    private void gameLoop(GameMechanics gameMechanics) {
        long beforeTime = System.nanoTime();
        long afterTime;
        long sleepTime;

        while (true) {
            gameMechanics.gameStep();

            afterTime = System.nanoTime();
            sleepTime = Math.max(0, TICK - (afterTime - beforeTime) / MICRO_SECOND);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                LOGGER.warn("Thread is interrupted");
            }
            beforeTime = System.nanoTime();
        }
    }
}
