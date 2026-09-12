package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateBooking() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createItem(1, true);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T10:00:00",
                                  "end": "2099-01-01T12:00:00",
                                  "itemId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.bookerId").value(2))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldRejectBookingByOwner() throws Exception {
        createUser("owner@example.com");
        createItem(1, true);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T10:00:00",
                                  "end": "2099-01-01T12:00:00",
                                  "itemId": 1
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectBookingForUnavailableItem() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createItem(1, false);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T10:00:00",
                                  "end": "2099-01-01T12:00:00",
                                  "itemId": 1
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404ForUnknownUser() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T10:00:00",
                                  "end": "2099-01-01T12:00:00",
                                  "itemId": 1
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForUnknownItem() throws Exception {
        createUser("booker@example.com");

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T10:00:00",
                                  "end": "2099-01-01T12:00:00",
                                  "itemId": 999
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidDates() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createItem(1, true);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T12:00:00",
                                  "end": "2099-01-01T10:00:00",
                                  "itemId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldGetBookingById() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createItem(1, true);
        createBooking(2, 1);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.bookerId").value(2));
    }

    @Test
    void shouldRejectAccessToForeignBooking() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createUser("stranger@example.com");
        createItem(1, true);
        createBooking(2, 1);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetBookerBookings() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createItem(1, true);
        createBooking(2, 1);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createItem(1, true);
        createBooking(2, 1);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldRejectBookingWithEqualDates() throws Exception {
        createUser("owner@example.com");
        createUser("booker@example.com");
        createItem(1, true);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T10:00:00",
                                  "end": "2099-01-01T10:00:00",
                                  "itemId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private void createUser(String email) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test User",
                                  "email": "%s"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    private void createItem(
            long ownerId,
            boolean available) throws Exception {

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Drill",
                                  "description": "Power drill",
                                  "available": %s
                                }
                                """.formatted(available)))
                .andExpect(status().isCreated());
    }

    private void createBooking(
            long bookerId,
            long itemId) throws Exception {

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"start": "2099-01-01T10:00:00",
                                  "end": "2099-01-01T12:00:00",
                                  "itemId": %d
                                }
                                """.formatted(itemId)))
                .andExpect(status().isCreated());
    }
}