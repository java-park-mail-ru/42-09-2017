package ru.mail.park.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.ResponseCodes;
import ru.mail.park.controllers.validators.Validator;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.response.ResponseBody;
import ru.mail.park.services.UserService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/auth")
public class UserController {
    private final UserService userService;

    private static final String SESSION_ATTR = "user_info";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("signup")
    public ResponseEntity<List<? extends ResponseBody>> signup(@RequestBody User userSignupInfo) {
        List<ResponseBody> response = new ArrayList<>();
        ResponseBody validateResult;
        Validator validator = new Validator(userService);

        validateResult = validator.validateUsername(userSignupInfo.getUsername());
        if (validateResult != null) {
            response.add(validateResult);
        }

        validateResult = validator.validateEmail(userSignupInfo.getEmail());
        if (validateResult != null) {
            response.add(validateResult);
        }

        validateResult = validator.validatePassword(userSignupInfo.getPassword());
        if (validateResult != null) {
            response.add(validateResult);
        }

        if (!response.isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }

        userService.addUser(userSignupInfo);
        response.add(new ResponseBody(ResponseCodes.SUCCESS, "Successfully signed up"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("update")
    public ResponseEntity<List<? extends ResponseBody>> update(@RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        List<ResponseBody> response = new ArrayList<>();
        ResponseBody validateResult;
        Validator validator = new Validator(userService);

        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            response.add(new ResponseBody(ResponseCodes.UNAUTHORIZED, "You are not authorized"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (userUpdateInfo.getUsername() != null) {
            validateResult = validator.validateUsername(userUpdateInfo.getUsername());
            if (validateResult != null) {
                response.add(validateResult);
            }
            httpSession.setAttribute(SESSION_ATTR, userUpdateInfo.getUsername());
        }
        if (userUpdateInfo.getEmail() != null) {
            validateResult = validator.validateEmail(userUpdateInfo.getEmail());
            if (validateResult != null) {
                response.add(validateResult);
            }
        }
        if (userUpdateInfo.getOldPassword() != null) {
            final String oldPassword = userUpdateInfo.getOldPassword();
            if (userService.checkUserAndPassword(username, oldPassword) == null) {
                response.add(new ResponseBody(ResponseCodes.PASSWORD_FIELD_BAD, "Wrong old password"));
            }
            if (userUpdateInfo.getPassword() != null) {
                final String newPassword = userUpdateInfo.getPassword();
                validateResult = validator.validatePassword(newPassword);
                if (validateResult != null) {
                    response.add(validateResult);
                }
            } else {
                response.add(new ResponseBody(ResponseCodes.PASSWORD_FIELD_BAD, "New password field is empty"));
            }
        } else if (userUpdateInfo.getPassword() != null) {
            response.add(new ResponseBody(ResponseCodes.PASSWORD_FIELD_BAD, "Old password is necessary for changing password"));
        }
        if (!response.isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }

        userService.updateUser(username, userUpdateInfo);
        response.add(new ResponseBody(ResponseCodes.SUCCESS, "Updated successfully"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("me")
    public ResponseEntity<? extends ResponseBody> me(HttpSession httpSession) {
        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseBody(ResponseCodes.UNAUTHORIZED, "You are not authorized")
            );
        }
        return ResponseEntity.ok(new ResponseBody(ResponseCodes.SUCCESS, username));
    }

    @PostMapping("login")
    public ResponseEntity<? extends ResponseBody> login(@RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        final User user = userService.checkUserAndPassword(userSigninInfo.getUsernameOrEmail(), userSigninInfo.getPassword());
        if (user == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseBody(ResponseCodes.USERNAME_OR_PASSWORD_WRONG, "Wrong username or password")
            );
        }
        httpSession.setAttribute(SESSION_ATTR, user.getUsername());
        return ResponseEntity.ok(new ResponseBody(ResponseCodes.SUCCESS, "Logged in"));
    }

    @GetMapping("logout")
    public ResponseEntity<? extends ResponseBody> logout(HttpSession httpSession) {
        httpSession.invalidate();
        return ResponseEntity.ok(new ResponseBody(ResponseCodes.SUCCESS, "Logged out"));
    }

}


