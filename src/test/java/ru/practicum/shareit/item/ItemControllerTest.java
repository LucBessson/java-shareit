package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTest {

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
    void shouldCreateItem() throws Exception {
        long userId = createUser("john@example.com");

        MvcResult result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.available").value(true))
                .andReturn();

        extractId(result);
    }

    @Test
    void shouldRejectItemWithoutAvailable() throws Exception {
        long userId = createUser("john@example.com");

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Power drill\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectItemWithoutName() throws Exception {
        long userId = createUser("john@example.com");

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Power drill\",\"available\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateUnavailableItem() throws Exception {
        long userId = createUser("john@example.com");

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        long userId = createUser("john@example.com");
        long itemId = createItem(userId);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated drill\",\"available\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated drill"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldReturnItem() throws Exception {
        long userId = createUser("john@example.com");
        long itemId = createItem(userId);

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldReturn404ForUnknownItem() throws Exception {
        mockMvc.perform(get("/items/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnOwnerItems() throws Exception {
        long userId = createUser("john@example.com");
        createItem(userId);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void shouldSearchItems() throws Exception {
        long userId = createUser("john@example.com");
        createItem(userId);

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    private long createUser(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private long createItem(long userId) throws Exception {
        MvcResult result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":true}"))
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