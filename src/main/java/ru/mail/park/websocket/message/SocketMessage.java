package ru.mail.park.websocket.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import ru.mail.park.websocket.message.from.MovingMessage;
import ru.mail.park.websocket.message.from.SnapMessage;
import ru.mail.park.websocket.message.from.StartMessage;
import ru.mail.park.websocket.message.from.SubscribeMessage;
import ru.mail.park.websocket.message.to.StartedMessage;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "class")
@JsonSubTypes({
        @Type(SubscribeMessage.class),
        @Type(MovingMessage.class),
        @Type(SnapMessage.class),
        @Type(StartMessage.class),
        @Type(StartedMessage.class)
})
public abstract class SocketMessage {
}
