package ru.mail.park;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.controllers.domain.User;
import ru.mail.park.controllers.messages.MessageConstants;
import ru.mail.park.dto.UserDTO;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.services.UserDao;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
public class UserControllerTest {
    @Autowired
    private UserDao userDao;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ModelMapper modelMapper;

    private User user;
    private static UserDTO userDTO = new UserDTO("testuser", "testemail@example.com", "testpassword");;

    @Before
    public void setup() {
        user = modelMapper.map(
                userDTO,
                User.class
        );
        userDao.createUser(user);
    }

    @Test
    public void testSignup_DuplicateUsername() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testemail2@example.com");
        user.setPassword("testpass");

        mockMvc
                .perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("USERNAME_ALREADY_EXISTS"));

    }


    @Test
    public void testSignup_DuplicateEmail() throws Exception {
        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("testemail@example.com");
        user.setPassword("testpass");

        mockMvc
                .perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("EMAIL_ALREADY_EXISTS"));

    }

    @Test
    public void testSignup_AlreadyAuthorized() throws Exception {
        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("testemail2@example.com");
        user.setPassword("testpass");

        mockMvc
                .perform(post("/api/auth/signup")
                        .sessionAttr(Constants.SESSION_ATTR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.AUTHORIZED));
    }

    @Test
    public void testUpdate_Unauthorized() throws Exception {
        User user = new User();
        user.setUsername("testUsernameUpdate");

        mockMvc
                .perform(put("/api/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.UNAUTHORIZED));
    }

    @Test
    public void testUpdate_UsernameUpdateSuccess() throws Exception {
        User userTest = new User();
        userTest.setUsername("testUsernameUpdate");

        mockMvc
                .perform(put("/api/auth/update")
                        .sessionAttr(Constants.SESSION_ATTR, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username")
                        .value(userTest.getUsername()))
                .andExpect(jsonPath("email")
                        .value(user.getEmail()));
    }

    @Test
    public void testLogin_Success() throws Exception {
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserSigninInfo(userDTO.getUsername(), userDTO.getPassword()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username")
                        .value(userDTO.getUsername()))
                .andExpect(jsonPath("email")
                        .value(userDTO.getEmail()));
    }

    @Test
    public void testLogin_UserNotExists() throws Exception {
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserSigninInfo("userNotExists", userDTO.getPassword()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.BAD_LOGIN_DATA));
    }

    @Test
    public void testLogin_BadLoginData() throws Exception {
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserSigninInfo(userDTO.getUsername(), "wrongpass"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.BAD_LOGIN_DATA));
    }
}