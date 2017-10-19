package ru.mail.park;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import ru.mail.park.controllers.domain.User;
import ru.mail.park.dto.UserDTO;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.services.UserDao;

import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SpyBeanUserControllerTest {
    @SpyBean
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void loginWithSpyBean() {
        User user = new User();
        user.setId(1L);
        user.setUsername("foo");
        user.setPassword(passwordEncoder.encode("bar"));

        doReturn(user).when(userDao).findUserByUsername(anyString());

        ResponseEntity<UserDTO> loginResp = restTemplate.postForEntity(
                "/api/auth/login",
                new UserSigninInfo("foo", "bar"),
                UserDTO.class
        );
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        List<String> cookies = loginResp.getHeaders().get("Set-Cookie");
        assertNotNull(cookies);
        assertFalse(cookies.isEmpty());

        UserDTO userResp = loginResp.getBody();
        assertNotNull(user);

        assertEquals("foo", userResp.getUsername());
        verify(userDao).findUserByUsername(anyString());
    }
}
