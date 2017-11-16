//package ru.mail.park.mechanics;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//@Service
//public class MechanicsExecutor implements Runnable {
//    private static final Logger LOGGER = LoggerFactory.getLogger(MechanicsExecutor.class);
//
//    private final Executor tickExecutor = Executors.newSingleThreadExecutor();
//
//    public MechanicsExecutor() {
//
//    }
//
//    @PostConstruct
//    public void initAfterStartup() {
//        tickExecutor.execute(this);
//    }
//
//    @Override
//    public void run() {
//        gameLoop();
//    }
//
//    private void gameLoop() {
//
//    }
//}
