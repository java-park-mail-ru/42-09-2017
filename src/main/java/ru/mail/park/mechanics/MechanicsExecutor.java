package ru.mail.park.mechanics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static ru.mail.park.info.constants.Constants.MICRO_SECOND;
import static ru.mail.park.info.constants.Constants.TICK;

@Service
public class MechanicsExecutor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MechanicsExecutor.class);
    private final GameMechanics gameMechanics;
    private final Executor tickExecutor = Executors.newSingleThreadExecutor();

    public MechanicsExecutor(
            GameMechanics gameMechanics
    ) {
        this.gameMechanics = gameMechanics;
    }

    @PostConstruct
    public void initAfterStartup() {
        tickExecutor.execute(this);
    }

    @Override
    public void run() {
        gameLoop();
    }

    private void gameLoop() {
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
