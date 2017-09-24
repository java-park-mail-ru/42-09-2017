package ru.mail.park.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.controllers.messages.UserMessage;
import ru.mail.park.controllers.validators.Validator;
import ru.mail.park.view.View;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.services.UserService;

import javax.servlet.http.HttpSession;

import static ru.mail.park.controllers.messages.MessageResources.*;

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
    public ResponseEntity<Message> signup(@RequestBody User userSignupInfo) {
        Message validateResult;

        validateResult = validator.validateUsername(userSignupInfo.getUsername());
        if (validateResult != null) {
            return ResponseEntity.badRequest().body(validateResult);
        }

        validateResult = validator.validateEmail(userSignupInfo.getEmail());
        if (validateResult != null) {
            return ResponseEntity.badRequest().body(validateResult);
        }

        validateResult = validator.validatePassword(userSignupInfo.getPassword());
        if (validateResult != null) {
            return ResponseEntity.badRequest().body(validateResult);
        }

        userService.addUser(userSignupInfo);
        return ResponseEntity.ok(SIGNED_UP.getMessage());
    }

    @PostMapping("update")
    public ResponseEntity<Message> update(@RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        Message validateResult;

        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED.getMessage());
        }

        if (userUpdateInfo.getUsername() != null) {
            validateResult = validator.validateUsername(userUpdateInfo.getUsername());
            if (validateResult != null) {
                return ResponseEntity.badRequest().body(validateResult);
            }
        }
        if (userUpdateInfo.getEmail() != null) {
            validateResult = validator.validateEmail(userUpdateInfo.getEmail());
            if (validateResult != null) {
                return ResponseEntity.badRequest().body(validateResult);
            }
        }
        if (userUpdateInfo.getOldPassword() != null) {
            final String oldPassword = userUpdateInfo.getOldPassword();
            if (userService.checkUserAndPassword(username, oldPassword) == null) {
                return ResponseEntity.badRequest().body(BAD_OLD_PASSWORD.getMessage());
            }
            if (userUpdateInfo.getPassword() != null) {
                final String newPassword = userUpdateInfo.getPassword();
                validateResult = validator.validatePassword(newPassword);
                if (validateResult != null) {
                    return ResponseEntity.badRequest().body(validateResult);
                }
            } else {
                return ResponseEntity.badRequest().body(EMPTY_PASSWORD.getMessage());
            }
        } else if (userUpdateInfo.getPassword() != null) {
            return ResponseEntity.badRequest().body(EMPTY_OLD_PASSWORD.getMessage());
        }

        userService.updateUser(username, userUpdateInfo);
        if (userUpdateInfo.getUsername() != null) {
            httpSession.setAttribute(SESSION_ATTR, userUpdateInfo.getUsername());
        }
        return ResponseEntity.ok(UPDATED.getMessage());
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
            return ResponseEntity.badRequest().body(BAD_LOGIN_DATA.getMessage());
        }
        httpSession.setAttribute(SESSION_ATTR, user.getUsername());
        return ResponseEntity.ok(LOGGED_IN.getMessage());
    }

    @GetMapping("logout")
    public ResponseEntity<Message> logout(HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_ATTR) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED.getMessage());
        }
        httpSession.invalidate();
        return ResponseEntity.ok(LOGGED_OUT.getMessage());
    }

}


