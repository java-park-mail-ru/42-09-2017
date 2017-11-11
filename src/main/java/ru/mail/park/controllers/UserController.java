package ru.mail.park.controllers;

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

//@CrossOrigin(origins = {
//        "https://sand42box.herokuapp.com",
//        "https://nightly-42.herokuapp.com",
//        "https://master-42.herokuapp.com",
//        "http://localhost:8080",
//        "http://127.0.0.1:8080",
//        "http://0.0.0.0",
//        "*"
//})
@RestController
@RequestMapping(path = "/api/auth")
public class UserController {
    private final UserDao userDao;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(
            UserDao userDao
    ) {
        this.userDao = userDao;
    }

    @PostMapping("signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserDto userSignupInfo, HttpSession httpSession) {
        if (httpSession.getAttribute(Constants.SESSION_ATTR) != null) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.AUTHORIZED));
        }

        User user = UserHelper.fromDto(userSignupInfo);
        userDao.createUser(user);
        httpSession.setAttribute(Constants.SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(UserHelper.toDto(user));
    }

    @PutMapping("update")
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        List<String> errors = new ArrayList<>();

        Long id = (Long) httpSession.getAttribute(Constants.SESSION_ATTR);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message<>(MessageConstants.UNAUTHORIZED));
        }

        String oldPassword = userUpdateInfo.getOldPassword();
        String password = userUpdateInfo.getPassword();

        User user = userDao.findUserById(id);

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

    @GetMapping("me")
    public ResponseEntity<?> me(HttpSession httpSession) {
        Long id = (Long) httpSession.getAttribute(Constants.SESSION_ATTR);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MessageConstants.UNAUTHORIZED);
        }
        return ResponseEntity
                .ok(UserHelper.toDto(userDao.findUserById(id)));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        if (httpSession.getAttribute(Constants.SESSION_ATTR) != null) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.AUTHORIZED));
        }

        User user = userDao.prepareSignIn(userSigninInfo);

        httpSession.setAttribute(Constants.SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(UserHelper.toDto(user));
    }

    @DeleteMapping("logout")
    public ResponseEntity<Message<String>> logout(HttpSession httpSession) {
        if (httpSession.getAttribute(Constants.SESSION_ATTR) == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Message<>(MessageConstants.UNAUTHORIZED));
        }
        httpSession.invalidate();
        return ResponseEntity.ok(new Message<>(MessageConstants.LOGGED_OUT));
    }

}


