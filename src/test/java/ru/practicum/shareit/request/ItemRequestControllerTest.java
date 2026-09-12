package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    void shouldCreateRequest() throws Exception {
        createUser();
        mockMvc.perform(post("/requests").header("X-Sharer-User-Id", 1).contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"I need a drill\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.description").value("I need a drill"));
    }

    @Test
    void shouldRejectEmptyDescription() throws Exception {
        createUser();
        mockMvc.perform(post("/requests").header("X-Sharer-User-Id", 1).contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUserRequests() throws Exception {
        createUser();
        createRequest();
        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1)).andExpect(status().isOk()).andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void shouldReturnRequestsExceptCurrentUser() throws Exception {
        createUser();
        createRequest();
        createSecondUser();
        createSecondRequest();
        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1)).andExpect(status().isOk()).andExpect(jsonPath("$.length()", is(1))).andExpect(jsonPath("$[0].description").value("Second request"));
    }

    private void createUser() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"John Doe\",\"email\":\"john@example.com\"}")).andExpect(status().isCreated());
    }

    private void createSecondUser() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}")).andExpect(status().isCreated());
    }

    private void createRequest() throws Exception {
        mockMvc.perform(post("/requests").header("X-Sharer-User-Id", 1).contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"I need a drill\"}")).andExpect(status().isCreated());
    }

    private void createSecondRequest() throws Exception {
        mockMvc.perform(post("/requests").header("X-Sharer-User-Id", 2).contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Second request\"}")).andExpect(status().isCreated());
    }
}