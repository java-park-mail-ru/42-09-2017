package ru.mail.park.controllers;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.account.UserSettings;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.OAuthCodeRequest;
import ru.mail.park.domain.dto.UserDto;
import ru.mail.park.domain.dto.helpers.UserHelper;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.services.UserDao;

import javax.servlet.http.HttpSession;

import java.util.List;

import static ru.mail.park.info.constants.Constants.*;

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
    public ResponseEntity<?> oauth(@RequestParam String code, HttpSession httpSession) {
        String accessToken = (String) httpSession.getAttribute(OAUTH_VK_ATTR);
        if (accessToken != null && userDao.findUserVkByToken(accessToken) != null) {
            return ResponseEntity
                    .badRequest()
                    .body(new Message<>(MessageConstants.AUTHORIZED));
        }
        try {
            UserAuthResponse userAuthResponse = vkApiClient
                    .oauth().userAuthorizationCodeFlow(
                            CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, code
                    ).execute();
            accessToken = userAuthResponse.getAccessToken();
            httpSession.setAttribute(OAUTH_VK_ATTR, accessToken);
            Integer userId = userAuthResponse.getUserId();
            User user = userDao.findUserVkById(userId);
            if (user != null) {
                userDao.updateUserVk(user, accessToken);
            } else {
                user = userDao.createUserVk(accessToken);
            }
            UserActor userActor = new UserActor(userAuthResponse.getUserId(), userAuthResponse.getAccessToken());
            List<UserXtrCounters> users = vkApiClient.users().get(userActor).userIds(userId.toString()).execute();
            UserDto userDto = UserHelper.toDto(user);
            userDto.setEmail(users.get(0).getLastName());
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
