package ru.mail.park.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.models.User;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.info.UserSignupInfo;
import ru.mail.park.View.View;
import ru.mail.park.response.ResponseBody;
import ru.mail.park.services.UserService;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(path = "/api/auth")
public class UserController {
    private UserService userService;
    private final String SessionAttr = "user_info";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("user")
    public String getUser() {
        return "{\"user\":\"user_name\"}";
    }

    @JsonView(View.Summary.class)
    @PostMapping("signup")
    public ResponseEntity<? extends ResponseBody> signup(@RequestBody UserSignupInfo userSignupInfo) {
        User user = userService.addUser(userSignupInfo);
        if(user != null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @PostMapping("update")
    public ResponseEntity<User> update(@RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SessionAttr);
        return ResponseEntity.ok(userService.updateUser(user, userUpdateInfo));
    }

    @JsonView(View.Summary.class)
    @PostMapping("me")
    public ResponseEntity<User> whoAmI(HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SessionAttr);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping(value = "login", consumes = "application/json")
    public ResponseEntity<String> login(@RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        User user = userService.checkUser(userSigninInfo);
        if (user == null) {
            return ResponseEntity.badRequest().body("Wrong username or password");
        }
        httpSession.setAttribute(SessionAttr, user);
        return ResponseEntity.ok("Logged in");
    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(HttpSession httpSession) {
        httpSession.invalidate();
        return ResponseEntity.ok("Logged out");
    }

}


