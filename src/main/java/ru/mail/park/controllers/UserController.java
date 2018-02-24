package ru.mail.park.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.domain.User;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.domain.dto.helpers.UserHelper;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.domain.dto.UserDto;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.services.UserDao;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/auth")
public class UserController {
    private final UserDao userDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public UserController(
            UserDao userDao
    ) {
        this.userDao = userDao;
    }

    @PostMapping("signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDto userSignupInfo, HttpSession httpSession) {
        if (httpSession.getAttribute(Constants.SESSION_ATTR) != null
                || httpSession.getAttribute(Constants.OAUTH_VK_ATTR) != null) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.AUTHORIZED));
        }

        final User user = UserHelper.fromDto(userSignupInfo);
        userDao.createUser(user);
        httpSession.setAttribute(Constants.SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(UserHelper.toDto(user));
    }

    @PutMapping("update")
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {

        final Long id = (Long) httpSession.getAttribute(Constants.SESSION_ATTR);
        final String vkToken = (String) httpSession.getAttribute(Constants.OAUTH_VK_ATTR);
        if (id == null && vkToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message<>(MessageConstants.UNAUTHORIZED));
        }

        final String oldPassword = userUpdateInfo.getOldPassword();
        final String password = userUpdateInfo.getPassword();

        final User user = userDao.findUserById(id);

        final List<String> errors = new ArrayList<>();
        if (oldPassword != null && password == null) {
            errors.add(MessageConstants.EMPTY_PASSWORD);
        } else if (oldPassword == null && password != null) {
            errors.add(MessageConstants.EMPTY_OLD_PASSWORD);
        } else if (oldPassword != null && !userDao.checkUserPassword(user, oldPassword)) {
            errors.add(MessageConstants.BAD_OLD_PASSWORD);
        }
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new Message<>(errors));
        }

        userDao.updateUser(user, userUpdateInfo);
        return ResponseEntity
                .ok(UserHelper.toDto(user));
    }

    @SuppressWarnings("InstanceMethodNamingConvention")
    @GetMapping("me")
    public ResponseEntity<?> me(HttpSession httpSession) {
        final Long id = (Long) httpSession.getAttribute(Constants.SESSION_ATTR);
        final String vkToken = (String) httpSession.getAttribute(Constants.OAUTH_VK_ATTR);
        if (id != null) {
            return ResponseEntity
                    .ok(UserHelper.toDto(userDao.findUserById(id)));
        } else if (vkToken != null) {
            try {
                LOGGER.warn(mapper.writeValueAsString(userDao.findUserVkByToken(vkToken)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return ResponseEntity
                    .ok(UserHelper.toDto(userDao.findUserVkByToken(vkToken)));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MessageConstants.UNAUTHORIZED);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        if (httpSession.getAttribute(Constants.SESSION_ATTR) != null
                || httpSession.getAttribute(Constants.OAUTH_VK_ATTR) != null) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.AUTHORIZED));
        }

        final User user = userDao.prepareSignIn(userSigninInfo);

        httpSession.setAttribute(Constants.SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(UserHelper.toDto(user));
    }

    @DeleteMapping("logout")
    public ResponseEntity<Message<String>> logout(HttpSession httpSession) {
        if (httpSession.getAttribute(Constants.SESSION_ATTR) == null
                && httpSession.getAttribute(Constants.OAUTH_VK_ATTR) == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Message<>(MessageConstants.UNAUTHORIZED));
        }
        httpSession.invalidate();
        return ResponseEntity.ok(new Message<>(MessageConstants.LOGGED_OUT));
    }

}


