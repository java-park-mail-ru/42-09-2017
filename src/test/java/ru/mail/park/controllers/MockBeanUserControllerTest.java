package ru.mail.park.controllers;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import ru.mail.park.domain.User;
import ru.mail.park.domain.dto.UserDto;
import ru.mail.park.info.UserSigninInfo;
import ru.mail.park.services.UserDao;

import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
public class MockBeanUserControllerTest {
    @MockBean
    private UserDao userDao;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void loginWithMockBean() {
        final User user = new User();
        user.setId(1L);
        user.setUsername("foo");

        when(userDao.prepareSignIn(any(UserSigninInfo.class))).thenReturn(user);

        final ResponseEntity<UserDto> loginResp = restTemplate.postForEntity(
                "/api/auth/login",
                new UserSigninInfo("foo", "bar"),
                UserDto.class
        );
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        final List<String> cookies = loginResp.getHeaders().get("Set-Cookie");
        assertNotNull(cookies);
        assertFalse(cookies.isEmpty());

        final UserDto userResp = loginResp.getBody();
        assertNotNull(user);

        assert userResp != null;
        assertEquals("foo", userResp.getUsername());
        verify(userDao).prepareSignIn(any(UserSigninInfo.class));
    }
}
