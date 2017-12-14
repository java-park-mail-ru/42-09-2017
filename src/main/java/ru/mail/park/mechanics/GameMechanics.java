package ru.mail.park.mechanics;

import ru.mail.park.domain.Board;
import ru.mail.park.domain.Id;
import ru.mail.park.domain.User;
import ru.mail.park.mechanics.objects.BodyFrame;
import ru.mail.park.websocket.message.from.SnapMessage;
import ru.mail.park.websocket.message.to.FinishedMessage;

import java.util.Set;

public interface GameMechanics {
    void gameStep();

    void addWaiter(Id<User> userId, Id<Board> board);

    void removeWaiter(Id<User> userId);

    void addBoardMessageTask(Set<Id<User>> players);

    void addMovingMessageTask(Id<User> from, BodyFrame snap);

    void addStartedMessageTask(GameSession session);

    void addSnapMessageTask(Id<User> userId, SnapMessage message);

    void addFinishedMessageTask(Id<User> userId, FinishedMessage message);

    void tryJoinGame();

    void tryStartSimulation();

    void processFinishedSimulation();

    void tryFinishGame();

    void userDisconnected(Id<User> userId);
}
