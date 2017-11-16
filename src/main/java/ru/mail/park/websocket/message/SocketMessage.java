package ru.mail.park.websocket.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "class")
@JsonSubTypes({
        @Type(SubscribeMessage.class),
        @Type(MovingMessage.class),
        @Type(InitMessage.class),
        @Type(StartMessage.class)
})
public abstract class SocketMessage {
}
