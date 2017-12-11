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
import ru.mail.park.domain.User;
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
        HttpHeaders headers = new HttpHeaders();
        try {
            URI redirectURI = new URI(RESULT_REDIRECT_URI);
            headers.setLocation(redirectURI);
            if (accessToken != null && userDao.findUserVkByToken(accessToken) != null) {
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            }
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
                userDao.createUserVk(userId, accessToken);
            }
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (ApiException | ClientException | URISyntaxException e) {
            e.printStackTrace();
            LOGGER.error("Vk or URI Syntax Exception");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }
}
