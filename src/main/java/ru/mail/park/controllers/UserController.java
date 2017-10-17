package ru.mail.park.controllers;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.controllers.domain.User;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.controllers.validators.Validator;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.dto.UserDTO;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.services.dao.UserDao;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = { "https://sand42box.herokuapp.com",
        "https://nightly-42.herokuapp.com",
        "https://master-42.herokuapp.com" }
        )
@RestController
@RequestMapping(path = "/api/auth")
public class UserController {
    private final UserDao userDao;
    private final Validator validator;
    private ModelMapper modelMapper;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final String SESSION_ATTR = "user_info";

    public UserController(
            UserDao userDao,
            Validator validator,
            ModelMapper modelMapper
    ) {
        this.userDao = userDao;
        this.validator = validator;
        this.modelMapper = modelMapper;
    }

    @PostMapping("signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserDTO userSignupInfo, HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_ATTR) != null) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.AUTHORIZED));
        }

        User user = modelMapper.map(userSignupInfo, User.class);
        userDao.createUser(user);
        httpSession.setAttribute(SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(modelMapper.map(user, UserDTO.class));
    }

    @PutMapping("update")
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        String validateResult;
        List<String> responseList = new ArrayList<>();

        Long id = (Long) httpSession.getAttribute(SESSION_ATTR);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message<>(MessageConstants.UNAUTHORIZED));
        }

        User user = userDao.findUserById(id);

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
            if (!userDao.checkUserPassword(user, oldPassword)) {
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

        userDao.updateUser(user, userUpdateInfo);
        return ResponseEntity
                .ok(modelMapper.map(user, UserDTO.class));
    }

    @GetMapping("me")
    public ResponseEntity<?> me(HttpSession httpSession) {
        Long id = (Long) httpSession.getAttribute(SESSION_ATTR);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity
                .ok(modelMapper.map(userDao.findUserById(id), UserDTO.class));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody UserSigninInfo userSigninInfo, HttpSession httpSession) {
        List<String> responseList = new ArrayList<>();
        User user = userDao.findUserByUsername(userSigninInfo.getLogin());
        if (user == null) {
            user = userDao.findUserByEmail(userSigninInfo.getLogin());
        }
        if (user == null || !userDao.checkUserPassword(user, userSigninInfo.getPassword())) {
            responseList.add(MessageConstants.BAD_LOGIN_DATA);
            return ResponseEntity
                    .badRequest()
                    .body(new Message<>(responseList));
        }
        httpSession.setAttribute(SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(modelMapper.map(user, UserDTO.class));
    }

    @DeleteMapping("logout")
    public ResponseEntity<Message<String>> logout(HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_ATTR) == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Message<>(MessageConstants.UNAUTHORIZED));
        }
        httpSession.invalidate();
        return ResponseEntity.ok(new Message<>(MessageConstants.LOGGED_OUT));
    }

}


