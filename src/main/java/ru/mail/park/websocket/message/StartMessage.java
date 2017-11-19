package ru.mail.park.websocket.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.validation.annotation.Validated;
import ru.mail.park.controllers.validators.groups.InitSnap;
import ru.mail.park.mechanics.objects.ClientSnap;

@Validated(InitSnap.class)
public class StartMessage extends SocketMessage {
    private ClientSnap snap;

    @JsonCreator
    public StartMessage() {

    }

    public StartMessage(
            ClientSnap snap
    ) {
        this.snap = snap;
    }

    public ClientSnap getSnap() {
        return snap;
    }

    public void setSnap(ClientSnap snap) {
        this.snap = snap;
    }
}
