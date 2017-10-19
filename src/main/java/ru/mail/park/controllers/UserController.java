package ru.mail.park.controllers;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.controllers.domain.User;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.controllers.validators.CValidatorChain;
import ru.mail.park.exceptions.ControllerValidationException;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.dto.UserDTO;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.services.UserDao;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = { "https://sand42box.herokuapp.com",
        "https://nightly-42.herokuapp.com",
        "https://master-42.herokuapp.com"
})
@RestController
@RequestMapping(path = "/api/auth")
public class UserController {
    private final UserDao userDao;
    private final CValidatorChain validatorChain;
    private ModelMapper modelMapper;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(
            UserDao userDao,
            CValidatorChain validatorChain,
            ModelMapper modelMapper
    ) {
        this.userDao = userDao;
        this.validatorChain = validatorChain;
        this.modelMapper = modelMapper;
    }

    @PostMapping("signup")
    public ResponseEntity<?> signup(@RequestBody UserDTO userSignupInfo, HttpSession httpSession) {
        if (httpSession.getAttribute(Constants.SESSION_ATTR) != null) {
            return ResponseEntity.badRequest().body(new Message<>(MessageConstants.AUTHORIZED));
        }

        validatorChain.validate(userSignupInfo, httpSession);

        User user = modelMapper.map(userSignupInfo, User.class);
        userDao.createUser(user);
        httpSession.setAttribute(Constants.SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(modelMapper.map(user, UserDTO.class));
    }

    @PutMapping("update")
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateInfo userUpdateInfo, HttpSession httpSession) {
        List<String> responseList = new ArrayList<>();

        Long id = (Long) httpSession.getAttribute(Constants.SESSION_ATTR);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message<>(MessageConstants.UNAUTHORIZED));
        }

        validatorChain.validate(userUpdateInfo, httpSession);
        if (userUpdateInfo.getOldPassword() != null && userUpdateInfo.getPassword() == null) {
            responseList.add(MessageConstants.EMPTY_PASSWORD);
            throw new ControllerValidationException(responseList);
        } else if (userUpdateInfo.getOldPassword() == null && userUpdateInfo.getPassword() != null) {
            responseList.add(MessageConstants.EMPTY_OLD_PASSWORD);
            throw new ControllerValidationException(responseList);
        }

        User user = userDao.findUserById(id);
        userDao.updateUser(user, userUpdateInfo);
        return ResponseEntity
                .ok(modelMapper.map(user, UserDTO.class));
    }

    @GetMapping("me")
    public ResponseEntity<?> me(HttpSession httpSession) {
        Long id = (Long) httpSession.getAttribute(Constants.SESSION_ATTR);
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
        httpSession.setAttribute(Constants.SESSION_ATTR, user.getId());
        return ResponseEntity
                .ok(modelMapper.map(user, UserDTO.class));
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


