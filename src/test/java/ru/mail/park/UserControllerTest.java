package ru.mail.park;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
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
import ru.mail.park.dto.UserDTO;
import ru.mail.park.services.dao.UserDao;

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

    @Before
    public void setup() {
        User user = modelMapper.map(
                new UserDTO("testuser", "testemail@example.com", "testpassword"),
                User.class
        );
        userDao.createUser(user);
    }

    @Test
    public void testSignup_DuplicateUsername() throws Exception {
        mockMvc

                .perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserDTO("testuser", "testemail2@example.com", "testpassword"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("USERNAME_ALREADY_EXISTS"));

    }


    @Test
    public void testSignup_DuplicateEmail() throws Exception {
        mockMvc

                .perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserDTO("testuser2", "testemail@example.com", "testpassword"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("EMAIL_ALREADY_EXISTS"));

    }
}