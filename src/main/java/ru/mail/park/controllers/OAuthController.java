package ru.mail.park.controllers;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.account.UserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.domain.dto.OAuthCodeRequest;
import ru.mail.park.domain.dto.UserDto;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.services.UserDao;

import static ru.mail.park.info.constants.Constants.CLIENT_ID;
import static ru.mail.park.info.constants.Constants.CLIENT_SECRET;
import static ru.mail.park.info.constants.Constants.REDIRECT_URI;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthController.class);
    private final UserDao userDao;
    private final VkApiClient vkApiClient;

    public OAuthController(
            UserDao userDao,
            VkApiClient vkApiClient
    ) {
        this.userDao = userDao;
        this.vkApiClient = vkApiClient;
    }

    @GetMapping("vk")
    public ResponseEntity<?> oauth(@RequestParam String code) {
        try {
            UserAuthResponse userAuthResponse = vkApiClient
                    .oauth().userAuthorizationCodeFlow(
                            CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, code
                    ).execute();
            UserActor userActor = new UserActor(userAuthResponse.getUserId(), userAuthResponse.getAccessToken());
            UserDto userDto = new UserDto();
            UserSettings userSettings = vkApiClient.account().getProfileInfo(userActor).execute();
            userDto.setUsername(userSettings.getFirstName());
            userDto.setEmail(userSettings.getLastName());
            return ResponseEntity
                    .ok(userDto);
        } catch (ApiException e) {
            e.printStackTrace();
            LOGGER.error("ApiException");
            return ResponseEntity
                    .badRequest()
                    .body(new Message<>(MessageConstants.VK_API_EXCEPTION));
        } catch (ClientException e) {
            e.printStackTrace();
            LOGGER.error("ClientException");
            return ResponseEntity
                    .badRequest()
                    .body(new Message<>(MessageConstants.VK_API_EXCEPTION));
        }
    }
}
