package ru.practicum.shareit.item;

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

        userRepository.findAll()
                .forEach(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    void shouldCreateItem() throws Exception {
        createUser();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Drill",
                                  "description": "Power drill",
                                  "available": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldRejectItemWithoutAvailable() throws Exception {
        createUser();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Drill",
                                  "description": "Power drill"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectItemWithoutName() throws Exception {
        createUser();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Power drill",
                                  "available": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateUnavailableItem() throws Exception {
        createUser();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Drill",
                                  "description": "Power drill",
                                  "available": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        createUser();
        createItem();

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated drill",
                                  "available": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated drill"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldReturnItem() throws Exception {
        createUser();
        createItem();

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldReturn404ForUnknownItem() throws Exception {
        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnOwnerItems() throws Exception {
        createUser();
        createItem();

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void shouldSearchItems() throws Exception {
        createUser();
        createItem();

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    private void createUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John Doe",
                                  "email": "john@example.com"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    private void createItem() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Drill",
                                  "description": "Power drill",
                                  "available": true
                                }
                                """))
                .andExpect(status().isCreated());
    }
}