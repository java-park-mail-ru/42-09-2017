package ru.mail.park;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;
import ru.mail.park.websocket.SocketHandler;

/**
 * Created by Artur on 07.09.2017.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public WebSocketHandler mySocketHandler() {
        return new PerConnectionWebSocketHandler(SocketHandler.class);
    }
}
