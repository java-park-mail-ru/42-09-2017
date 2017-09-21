package ru.mail.park.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.controllers.validators.Validator;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.services.UserService;

import javax.servlet.http.HttpSession;

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
    public ResponseEntity<String> signup(@RequestBody User userSignupInfo) {
        String validateResult;
        Validator validator = new Validator(userService);

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
        return ResponseEntity.ok("Successfully signed up");
    }

    @PostMapping("update")
    public ResponseEntity<String> update(@RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        String validateResult;
        Validator validator = new Validator(userService);

        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized");
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
                return ResponseEntity.badRequest().body("Wrong old password");
            }
            if (userUpdateInfo.getPassword() != null) {
                final String newPassword = userUpdateInfo.getPassword();
                validateResult = validator.validatePassword(newPassword);
                if (validateResult != null) {
                    return ResponseEntity.badRequest().body(validateResult);
                }
            } else {
                return ResponseEntity.badRequest().body("New password field is empty");
            }
        } else if (userUpdateInfo.getPassword() != null) {
            return ResponseEntity.badRequest().body("Old password is necessary for changing password");
        }

        userService.updateUser(username, userUpdateInfo);
        if (userUpdateInfo.getUsername() != null) {
            httpSession.setAttribute(SESSION_ATTR, userUpdateInfo.getUsername());
        }
        return ResponseEntity.ok("Updated successfully");
    }

    @GetMapping("me")
    public ResponseEntity<String> me(HttpSession httpSession) {
        String username = (String) httpSession.getAttribute(SESSION_ATTR);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized");
        }
        return ResponseEntity.ok(username);
    }

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        final User user = userService.checkUserAndPassword(userSigninInfo.getUsernameOrEmail(), userSigninInfo.getPassword());
        if (user == null) {
            return ResponseEntity.badRequest().body("Wrong username or password");
        }
        httpSession.setAttribute(SESSION_ATTR, user.getUsername());
        return ResponseEntity.ok("Logged in");
    }

    @GetMapping("logout")
    public ResponseEntity<String> logout(HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_ATTR) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized");
        }
        httpSession.invalidate();
        return ResponseEntity.ok("Logged out");
    }

}


