package ru.mail.park.mechanics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbox2d.common.Vec2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.BoardRequest;
import ru.mail.park.domain.dto.UserDto;
import ru.mail.park.domain.dto.helpers.BoardMetaHelper;
import ru.mail.park.domain.dto.helpers.UserHelper;
import ru.mail.park.mechanics.domain.objects.BodyFrame;
import ru.mail.park.services.GameDao;
import ru.mail.park.services.UserDao;
import ru.mail.park.websocket.message.from.SnapMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static ru.mail.park.info.constants.Constants.THREAD_POOL_SIZE;
import static ru.mail.park.info.constants.Constants.TICK;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class GameMechanicsServiceTest {
    @Autowired
    private GameSessionService gameSessionService;
    @Autowired
    private GameMechanicsService gameMechanicsService;
    @MockBean
    private UserDao userDao;
    @MockBean
    private GameDao gameDao;
    @MockBean
    private RemotePointService remotePointService;

    private final ObjectMapper mapper = new ObjectMapper();

    private BoardRequest boardRequest;
    private Id<Board> boardId = Id.of(1);

    private static final ScheduledExecutorService tickExecutor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMechanicsServiceTest.class);
    private final Map<Id<GameMechanics>, GameMechanics> mechanicsMap = new ConcurrentHashMap<>();

    private User user;

    public void setup() {
        mechanicsMap.clear();
        final Set<GameMechanics> mechanicsSet = gameMechanicsService.initMechanics(2);
        mechanicsSet.forEach(mechanics ->  {
            mechanicsMap.put(((GameMechanicsImpl) mechanics).getId(), mechanics);
            tickExecutor.scheduleAtFixedRate(mechanics::gameStep,
                    0, TICK, TimeUnit.MILLISECONDS);
        });
        final UserDto userDto = new UserDto(
                "testuser",
                "testemail@example.com",
                "testpassword");
        user = UserHelper.fromDto(userDto);
        user.setId(1L);
        try {
            final String testBoard = "{\"meta\":{\"level\":2,\"timer\":15,\"preview\":\"localhost\",\"name\":\"testBoard\",\"players\":1},\"data\":{\"bodies\":["
                    + "{\"id\":0,\"data\":{\"size\":null,\"type\":2,\"angle\":0,\"config\":null,\"radius\":0.6666667,"
                    + "\"options\":{\"sensor\":false,\"density\":0.2,\"friction\":0.3,\"keyBodyID\":2,\"restitution\":0.5},"
                    + "\"position\":{\"x\":30,\"y\":13}},\"kind\":\"circle\",\"playerID\":null,\"selectable\":false},"
                    + "{\"id\":1,\"data\":{\"size\":{\"x\":1,\"y\":1},\"type\":0,\"angle\":0,"
                    + "\"config\":{\"height\":6,\"bottomLength\":2.8,\"wallThickness\":0.6},\"radius\":null,"
                    + "\"options\":{\"sensor\":true,\"density\":0.2,\"friction\":0.3,\"keyBodyID\":2,\"restitution\":0.1},"
                    + "\"position\":{\"x\":30,\"y\":18}},\"kind\":\"bucket\",\"playerID\":null,\"selectable\":true}],\"joints\":[]}}";
            boardRequest = mapper.readValue(testBoard, BoardRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testHandleSubscribe() {
        setup();

        doReturn(user).when(userDao).findUserById(any());
        doReturn(boardRequest.getBoardData()).when(gameDao).getBoard(any());
        doReturn(BoardMetaHelper.fromDto(boardRequest.getBoardMetaDto())).when(gameDao).getMetaOf(any());
        boolean subscribed = false;
        if (boardId != null) {
            subscribed = gameMechanicsService.handleSubscribe(
                    Id.of(user.getId()),
                    boardId);
        }
        assertTrue(subscribed);
    }

    public void testHandleMoving() throws InterruptedException {
        testHandleSubscribe();
        Thread.sleep(1000);

        final boolean moved = gameMechanicsService.handleMoving(Id.of(user.getId()), new BodyFrame());
        assertTrue(moved);
        final GameSession session = gameSessionService.getSessionFor(Id.of(user.getId()));
        assertEquals(GameState.MOVING, session.getState());
    }

    public void testHandleStart() throws InterruptedException {
        testHandleMoving();
        Thread.sleep(1000);

        gameMechanicsService.handleStart(Id.of(user.getId()), new ArrayList<>());
    }

    public void testHandleSnap() throws InterruptedException {
        testHandleStart();
        Thread.sleep(1000);

        final SnapMessage snap = new SnapMessage();
        snap.setFrame(1L);
        final BodyFrame bodyFrame = new BodyFrame();
        bodyFrame.setId(0L);
        bodyFrame.setPosition(new Vec2(0f, 0f));
        bodyFrame.setLinVelocity(new Vec2(0f, 0f));
        bodyFrame.setAngle(0f);
        bodyFrame.setAngVelocity(0f);
        final List<BodyFrame> bodies = new ArrayList<>();
        bodies.add(bodyFrame);
        snap.setBodies(bodies);
        final boolean checked = gameMechanicsService.handleSnap(Id.of(user.getId()), snap);
        assertTrue(checked);

        final GameSession session = gameSessionService.getSessionFor(Id.of(user.getId()));
        assertEquals("SUCCESS", session.getResult());
        assertEquals(GameState.HANDLING, session.getState());
    }

    public void testHandleFinish() throws InterruptedException {
        testHandleSnap();
        Thread.sleep(100);

        final boolean finished = gameMechanicsService.handleFinish(Id.of(user.getId()));
        assertTrue(finished);
    }

    @Test
    public void testMechanics() {
        doReturn(true).when(remotePointService).isConnected(any());
        try {
            testHandleFinish();
        } catch (InterruptedException ignore) {
        }
    }
}
