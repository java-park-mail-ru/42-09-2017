package ru.mail.park.controllers;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.OAuthUserDto;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.services.UserDao;

import javax.servlet.http.HttpSession;

import java.net.URI;
import java.net.URISyntaxException;

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
                user = userDao.createUserVk(userId, accessToken);
            }
            OAuthUserDto userDto = new OAuthUserDto();
            userDto.setUserId(userId);
            userDto.setUsername(user.getUsername());
            HttpHeaders headers = new HttpHeaders();
            URI redirectURI = new URI("https://physicsio.tech/vk_ok");
            headers.setLocation(redirectURI);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (ApiException e) {
            e.printStackTrace();
            LOGGER.error("ApiException");
            return ResponseEntity
                    .badRequest()
                    .body(new Message<>(MessageConstants.VK_API_EXCEPTION));
        } catch (ClientException | URISyntaxException e) {
            e.printStackTrace();
            LOGGER.error("ClientException");
            return ResponseEntity
                    .badRequest()
                    .body(new Message<>(MessageConstants.VK_API_EXCEPTION));
        }
    }
}
