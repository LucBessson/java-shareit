package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.item.ItemRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        String json = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldRejectUserWithoutEmail() throws Exception {
        String json = "{\"name\":\"John Doe\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        String json = "{\"name\":\"John Doe\",\"email\":\"john.com\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        createUser("john@example.com");

        String json = "{\"name\":\"Another John\",\"email\":\"john@example.com\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUpdateUserEmail() throws Exception {
        long userId = createUser("old@example.com");
        String json = "{\"email\":\"new@example.com\"}";

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void shouldUpdateUserName() throws Exception {
        long userId = createUser("john@example.com");
        String json = "{\"name\":\"Updated John\"}";

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Updated John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldReturn404ForUnknownUser() throws Exception {
        mockMvc.perform(get("/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        createUser("one@example.com");
        createUser("two@example.com");

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        long userId = createUser("john@example.com");

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    private long createUser(String email) throws Exception {
        String json = "{\"name\":\"John Doe\",\"email\":\"" + email + "\"}";

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private long extractId(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();

        int idStart = json.indexOf("\"id\":") + 5;
        int idEnd = json.indexOf(",", idStart);

        if (idEnd == -1) {
            idEnd = json.indexOf("}", idStart);
        }

        return Long.parseLong(json.substring(idStart, idEnd).trim());
    }
}