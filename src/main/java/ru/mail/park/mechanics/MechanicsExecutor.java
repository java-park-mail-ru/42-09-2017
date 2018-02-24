package ru.mail.park.mechanics;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.mail.park.info.constants.Constants.THREAD_POOL_SIZE;
import static ru.mail.park.info.constants.Constants.TICK;

@Service
public class MechanicsExecutor {
    private final GameMechanicsService gameMechanicsService;
    private final ScheduledExecutorService tickExecutor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    public MechanicsExecutor(
            GameMechanicsService gameMechanicsService
    ) {
        this.gameMechanicsService = gameMechanicsService;
    }

    @PostConstruct
    public void initAfterStartup() {
        final Set<GameMechanics> mechanicsSet = gameMechanicsService.initMechanics(THREAD_POOL_SIZE);
        mechanicsSet.forEach(mechanics -> tickExecutor.scheduleAtFixedRate(mechanics::gameStep,
                0, TICK, TimeUnit.MILLISECONDS));
    }
}
