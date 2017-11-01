package ru.mail.park;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.domain.User;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.services.UserDao;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
public class UserControllerTest {
    @MockBean
    private UserDao userDao;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testSignup_DuplicateUsername() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testemail2@example.com");
        user.setPassword("testpass");

        when(userDao.hasUsername(anyString())).thenReturn(true);
        when(userDao.hasEmail(anyString())).thenReturn(false);
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

        when(userDao.hasUsername(anyString())).thenReturn(false);
        when(userDao.hasEmail(anyString())).thenReturn(true);
        mockMvc
                .perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("EMAIL_ALREADY_EXISTS"));

    }

    @SuppressWarnings("Duplicates")
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

    @SuppressWarnings("Duplicates")
    @Test
    public void testUpdate_Unauthorized() throws Exception {
        User userTest = new User();
        userTest.setUsername("testUsernameUpdate");

        mockMvc
                .perform(put("/api/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userTest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.UNAUTHORIZED));
    }

    @Test
    public void testUpdate_UsernameUpdateSuccess() throws Exception {
        User userTest = new User();
        userTest.setUsername("testUsernameUpdate");

        when(userDao.hasUsername(anyString())).thenReturn(false);
        when(userDao.findUserById(anyLong())).thenReturn(userTest);
        when(userDao.updateUser(any(User.class), any(UserUpdateInfo.class))).thenReturn(userTest);
        mockMvc
                .perform(put("/api/auth/update")
                        .sessionAttr(Constants.SESSION_ATTR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username")
                        .value(userTest.getUsername()));
    }

    @Test
    public void testLogin_Success() throws Exception {
        User userTest = new User();
        userTest.setUsername("login");
        userTest.setEmail("email");

        when(userDao.hasUsername(anyString())).thenReturn(true);
        when(userDao.findUserByUsername(anyString())).thenReturn(userTest);
        when(userDao.checkUserPassword(any(User.class), anyString())).thenReturn(true);
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserSigninInfo(userTest.getUsername(), "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username")
                        .value(userTest.getUsername()))
                .andExpect(jsonPath("email")
                        .value(userTest.getEmail()));
    }

    @Test
    public void testLogin_UserNotExists() throws Exception {
        when(userDao.hasUsername(anyString())).thenReturn(false);
        when(userDao.hasEmail(anyString())).thenReturn(false);
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserSigninInfo("userNotExists", "password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.USERNAME_NOT_EXISTS));
    }

    @Test
    public void testLogin_BadLoginData() throws Exception {
        when(userDao.hasUsername(anyString())).thenReturn(true);
        when(userDao.findUserByUsername(anyString())).thenReturn(new User());
        when(userDao.checkUserPassword(any(User.class), anyString())).thenReturn(false);
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserSigninInfo("username", "wrongPassword"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.PASSWORD_WRONG));
    }

    @Test
    public void testMe_Success() throws Exception {
        User userTest = new User();
        userTest.setUsername("username");
        userTest.setEmail("email");

        when(userDao.findUserById(anyLong())).thenReturn(userTest);
        mockMvc
                .perform(get("/api/auth/me")
                        .sessionAttr(Constants.SESSION_ATTR, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username")
                        .value(userTest.getUsername()))
                .andExpect(jsonPath("email")
                        .value(userTest.getEmail()));
    }

    @Test
    public void testMe_Unauthorized() throws Exception {
        mockMvc
                .perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}