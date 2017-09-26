package ru.mail.park.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.controllers.messages.UserMessage;
import ru.mail.park.controllers.validators.Validator;
import ru.mail.park.view.View;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.services.UserService;

import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "https://sand42box.herokuapp.com")
@RestController
@RequestMapping(path = "/api/auth")
public class UserController {
    private final UserService userService;
    private final Validator validator;

    private static final String SESSION_ATTR = "user_info";

    public UserController(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @PostMapping("signup")
    public ResponseEntity<Message<?>> signup(@RequestBody User userSignupInfo) {
        String validateResult;
        List<String> responseList = new ArrayList<>();

        validateResult = validator.validateUsername(userSignupInfo.getUsername());
        if (validateResult != null) {
            responseList.add(validateResult);
        }

        validateResult = validator.validateEmail(userSignupInfo.getEmail());
        if (validateResult != null) {
            responseList.add(validateResult);
        }

        validateResult = Validator.validatePassword(userSignupInfo.getPassword());
        if (validateResult != null) {
            responseList.add(validateResult);
        }

        if (!responseList.isEmpty()) {
            return ResponseEntity.badRequest().body(new Message<>(responseList));
        }

        userService.addUser(userSignupInfo);
        return ResponseEntity.ok(new Message<>(MessageConstants.SIGNED_UP));
    }

    @PostMapping("update")
    @JsonView(View.SummaryWithMessage.class)
    public ResponseEntity<? extends Message<?>> update(@RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        String validateResult;
        List<String> responseList = new ArrayList<>();

        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message<>(MessageConstants.UNAUTHORIZED));
        }

        if (userUpdateInfo.getUsername() != null) {
            validateResult = validator.validateUsername(userUpdateInfo.getUsername());
            if (validateResult != null) {
                responseList.add(validateResult);
            }
        }
        if (userUpdateInfo.getEmail() != null) {
            validateResult = validator.validateEmail(userUpdateInfo.getEmail());
            if (validateResult != null) {
                responseList.add(validateResult);
            }
        }
        if (userUpdateInfo.getOldPassword() != null) {
            final String oldPassword = userUpdateInfo.getOldPassword();
            if (userService.checkUserAndPassword(username, oldPassword) == null) {
                responseList.add(MessageConstants.BAD_OLD_PASSWORD);
            }
            if (userUpdateInfo.getPassword() != null) {
                final String newPassword = userUpdateInfo.getPassword();
                validateResult = Validator.validatePassword(newPassword);
                if (validateResult != null) {
                    responseList.add(validateResult);
                }
            } else {
                responseList.add(MessageConstants.EMPTY_PASSWORD);
            }
        } else if (userUpdateInfo.getPassword() != null) {
            responseList.add(MessageConstants.EMPTY_OLD_PASSWORD);
        }

        if (!responseList.isEmpty()) {
            return ResponseEntity.badRequest().body(new Message<>(responseList));
        }

        User user = userService.updateUser(username, userUpdateInfo);
        if (userUpdateInfo.getUsername() != null) {
            httpSession.setAttribute(SESSION_ATTR, userUpdateInfo.getUsername());
        }
        return ResponseEntity.ok(new UserMessage<>(MessageConstants.UPDATED, user));
    }

    @GetMapping("me")
    @JsonView(View.Summary.class)
    public ResponseEntity<?> me(HttpSession httpSession) {
        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        return ResponseEntity.ok(new UserMessage<>(null, userService.getByUsername(username)));
    }

    @PostMapping("login")
    public ResponseEntity<Message<String>> login(@RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        if (userSigninInfo.getLogin() == null || userSigninInfo.getLogin().isEmpty()) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.EMPTY_USERNAME));
        }
        if (userSigninInfo.getPassword() == null || userSigninInfo.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.EMPTY_PASSWORD));
        }

        final User user = userService.checkUserAndPassword(userSigninInfo.getLogin(), userSigninInfo.getPassword());
        if (user == null) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.BAD_LOGIN_DATA));
        }
        httpSession.setAttribute(SESSION_ATTR, user.getUsername());
        return ResponseEntity.ok(new Message<>(MessageConstants.LOGGED_IN));
    }

    @GetMapping("logout")
    public ResponseEntity<Message<String>> logout(HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_ATTR) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message<>(MessageConstants.UNAUTHORIZED));
        }
        httpSession.invalidate();
        return ResponseEntity.ok(new Message<>(MessageConstants.LOGGED_OUT));
    }

}


