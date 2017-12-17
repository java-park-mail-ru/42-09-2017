package ru.mail.park;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.domain.User;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.domain.dto.UserDto;
import ru.mail.park.domain.dto.helpers.UserHelper;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.info.UserUpdateInfo;
import ru.mail.park.info.constants.Constants;
import ru.mail.park.services.UserDao;

@SuppressWarnings("InstanceMethodNamingConvention")
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
public class UserDaoTest {
    @SpyBean
    private UserDao userDao;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private User user;
    private static final UserDto userDto = new UserDto("testuser", "testemail@example.com", "testpassword");

    @Before
    public void setup() {
        user = UserHelper.fromDto(userDto);
        userDao.createUser(user);
    }

    @Test
    public void testSignup_DuplicateUsername() throws Exception {
        final User userTest = new User();
        userTest.setUsername("testuser");
        userTest.setEmail("testemail2@example.com");
        userTest.setPassword("testpass");

        mockMvc
                .perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userTest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("USERNAME_ALREADY_EXISTS"));

        // Two invocations for each because there is createUser(User.class) invocation
        // (and so validation) in @Before method above
        verify(userDao, times(2)).createUser(any(User.class));
        verify(userDao, times(2)).checkIfNotExists(anyString(), anyString());
        verify(userDao, times(2)).hasUsername(anyString());
        verify(userDao, times(2)).hasEmail(anyString());
    }


    @Test
    public void testSignup_DuplicateEmail() throws Exception {
        final User userTest = new User();
        userTest.setUsername("testuser2");
        userTest.setEmail("testemail@example.com");
        userTest.setPassword("testpass");

        mockMvc
                .perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userTest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("EMAIL_ALREADY_EXISTS"));

        // Two invocations for each because there is createUser(User.class) invocation
        // (and so validation) in @Before method above
        verify(userDao, times(2)).createUser(any(User.class));
        verify(userDao, times(2)).checkIfNotExists(anyString(), anyString());
        verify(userDao, times(2)).hasUsername(anyString());
        verify(userDao, times(2)).hasEmail(anyString());
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void testSignup_AlreadyAuthorized() throws Exception {
        final User userTest = new User();
        userTest.setUsername("testuser2");
        userTest.setEmail("testemail2@example.com");
        userTest.setPassword("testpass");

        mockMvc
                .perform(post("/api/auth/signup")
                        .sessionAttr(Constants.SESSION_ATTR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userTest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.AUTHORIZED));
    }

    @Test
    public void testUpdate_Unauthorized() throws Exception {
        final User userTest = new User();
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
        final User userTest = new User();
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

        verify(userDao).findUserById(anyLong());
        verify(userDao).updateUser(any(User.class), any(UserUpdateInfo.class));
        verify(userDao).checkIfNotExists(anyString(), anyString());
        // Two invocations because there is createUser(User.class) invocation
        // (and so validation) in @Before method above
        verify(userDao, times(2)).hasUsername(anyString());
    }

    @Test
    public void testLogin_Success() throws Exception {
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserSigninInfo(userDto.getUsername(), userDto.getPassword()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username")
                        .value(userDto.getUsername()))
                .andExpect(jsonPath("email")
                        .value(userDto.getEmail()));

        verify(userDao).findUserByUsername(anyString());
        verify(userDao).checkUserPassword(any(User.class), anyString());
    }

    @Test
    public void testLogin_UserNotExists() throws Exception {
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserSigninInfo("userNotExists", userDto.getPassword()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.USERNAME_NOT_EXISTS));

        // Two invocations for each because there is createUser(User.class) invocation
        // (and so validation) in @Before method above
        verify(userDao, times(2)).hasUsername(anyString());
        verify(userDao, times(2)).hasEmail(anyString());
    }

    @Test
    public void testLogin_BadLoginData() throws Exception {
        mockMvc
                .perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserSigninInfo(userDto.getEmail(), "wrongpass"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message")
                        .value(MessageConstants.PASSWORD_WRONG));

        verify(userDao).findUserByUsername(anyString());
        verify(userDao).findUserByEmail(anyString());
        verify(userDao).checkUserPassword(any(User.class), anyString());
    }

    @Test
    public void testMe_Success() throws Exception {
        mockMvc
                .perform(get("/api/auth/me")
                        .sessionAttr(Constants.SESSION_ATTR, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username")
                        .value(user.getUsername()))
                .andExpect(jsonPath("email")
                        .value(user.getEmail()));

        verify(userDao).findUserById(anyLong());
    }

    @Test
    public void testMe_Unauthorized() throws Exception {
        mockMvc
                .perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}