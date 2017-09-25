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

    private static final String SESSION_ATTR = "user_info";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("signup")
    public ResponseEntity<List<Message>> signup(@RequestBody User userSignupInfo) {
        Message validateResult;
        List<Message> responseList = new ArrayList<>();

        validateResult = Validator.validateUsername(userSignupInfo.getUsername());
        if (validateResult != null) {
            responseList.add(validateResult);
        }

        validateResult = Validator.validateEmail(userSignupInfo.getEmail());
        if (validateResult != null) {
            responseList.add(validateResult);
        }

        validateResult = Validator.validatePassword(userSignupInfo.getPassword());
        if (validateResult != null) {
            responseList.add(validateResult);
        }

        if (!responseList.isEmpty()) {
            return ResponseEntity.badRequest().body(responseList);
        }

        userService.addUser(userSignupInfo);
        responseList.add(new Message(MessageConstants.SIGNED_UP));
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("update")
    public ResponseEntity<List<Message>> update(@RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        Message validateResult;
        List<Message> responseList = new ArrayList<>();

        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            responseList.add(new Message(MessageConstants.UNAUTHORIZED));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseList);
        }

        if (userUpdateInfo.getUsername() != null) {
            validateResult = Validator.validateUsername(userUpdateInfo.getUsername());
            if (validateResult != null) {
                responseList.add(validateResult);
            }
        }
        if (userUpdateInfo.getEmail() != null) {
            validateResult = Validator.validateEmail(userUpdateInfo.getEmail());
            if (validateResult != null) {
                responseList.add(validateResult);
            }
        }
        if (userUpdateInfo.getOldPassword() != null) {
            final String oldPassword = userUpdateInfo.getOldPassword();
            if (userService.checkUserAndPassword(username, oldPassword) == null) {
                responseList.add(new Message(MessageConstants.BAD_OLD_PASSWORD));
            }
            if (userUpdateInfo.getPassword() != null) {
                final String newPassword = userUpdateInfo.getPassword();
                validateResult = Validator.validatePassword(newPassword);
                if (validateResult != null) {
                    responseList.add(validateResult);
                }
            } else {
                responseList.add(new Message(MessageConstants.EMPTY_PASSWORD));
            }
        } else if (userUpdateInfo.getPassword() != null) {
            responseList.add(new Message(MessageConstants.EMPTY_OLD_PASSWORD));
        }

        if (!responseList.isEmpty()) {
            return ResponseEntity.badRequest().body(responseList);
        }

        userService.updateUser(username, userUpdateInfo);
        if (userUpdateInfo.getUsername() != null) {
            httpSession.setAttribute(SESSION_ATTR, userUpdateInfo.getUsername());
        }
        responseList.add(new Message(MessageConstants.UPDATED));
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("me")
    @JsonView(View.Summary.class)
    public ResponseEntity<UserMessage> me(HttpSession httpSession) {
        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserMessage(null));
        }

        return ResponseEntity.ok(new UserMessage(userService.getByUsername(username)));
    }

    @PostMapping("login")
    public ResponseEntity<Message> login(@RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        final User user = userService.checkUserAndPassword(userSigninInfo.getLogin(), userSigninInfo.getPassword());
        if (user == null) {
            return ResponseEntity.badRequest().body(new Message(MessageConstants.BAD_LOGIN_DATA));
        }
        httpSession.setAttribute(SESSION_ATTR, user.getUsername());
        return ResponseEntity.ok(new Message(MessageConstants.LOGGED_IN));
    }

    @GetMapping("logout")
    public ResponseEntity<Message> logout(HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_ATTR) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(MessageConstants.UNAUTHORIZED));
        }
        httpSession.invalidate();
        return ResponseEntity.ok(new Message(MessageConstants.LOGGED_OUT));
    }

}


