package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);

        String json = "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T12:00:00\",\"itemId\":" + itemId + "}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldRejectBookingByOwner() throws Exception {
        long ownerId = createUser("owner@example.com");
        long itemId = createItem(ownerId, true);

        String json = "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T12:00:00\",\"itemId\":" + itemId + "}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectBookingForUnavailableItem() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, false);

        String json = "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T12:00:00\",\"itemId\":" + itemId + "}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForUnknownUser() throws Exception {
        String json = "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T12:00:00\",\"itemId\":999999}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForUnknownItem() throws Exception {
        long bookerId = createUser("booker@example.com");

        String json = "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T12:00:00\",\"itemId\":999999}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidDates() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);

        String json = "{\"start\":\"2099-01-01T12:00:00\",\"end\":\"2099-01-01T10:00:00\",\"itemId\":" + itemId + "}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBookingById() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);
        long bookingId = createBooking(bookerId, itemId);

        mockMvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andExpect(jsonPath("$.booker.id").value(bookerId));
    }

    @Test
    void shouldRejectAccessToForeignBooking() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long strangerId = createUser("stranger@example.com");
        long itemId = createItem(ownerId, true);
        long bookingId = createBooking(bookerId, itemId);

        mockMvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", strangerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetBookerBookings() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);
        long bookingId = createBooking(bookerId, itemId);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").value(bookingId))
                .andExpect(jsonPath("$[0].item.id").value(itemId))
                .andExpect(jsonPath("$[0].booker.id").value(bookerId));
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);
        long bookingId = createBooking(bookerId, itemId);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").value(bookingId))
                .andExpect(jsonPath("$[0].item.id").value(itemId))
                .andExpect(jsonPath("$[0].booker.id").value(bookerId));
    }

    @Test
    void shouldRejectBookingWithEqualDates() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);

        String json = "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T10:00:00\",\"itemId\":" + itemId + "}";

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldApproveBooking() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);
        long bookingId = createBooking(bookerId, itemId);

        mockMvc.perform(patch("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldRejectApprovalByBooker() throws Exception {
        long ownerId = createUser("owner@example.com");
        long bookerId = createUser("booker@example.com");
        long itemId = createItem(ownerId, true);
        long bookingId = createBooking(bookerId, itemId);

        mockMvc.perform(patch("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", bookerId)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForUnknownBooking() throws Exception {
        long ownerId = createUser("owner@example.com");

        mockMvc.perform(get("/bookings/999999")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isNotFound());
    }

    private long createUser(String email) throws Exception {
        String json = "{\"name\":\"Test User\",\"email\":\"" + email + "\"}";

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private long createItem(long ownerId, boolean available) throws Exception {
        String json = "{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":" + available + "}";

        MvcResult result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private long createBooking(long bookerId, long itemId) throws Exception {
        String json = "{\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T12:00:00\",\"itemId\":" + itemId + "}";

        MvcResult result = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
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