package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdAndEndIsBefore(
            Long bookerId,
            LocalDateTime end,
            Sort sort);

    List<Booking> findByBooker_IdAndStartIsAfter(
            Long bookerId,
            LocalDateTime start,
            Sort sort);

    List<Booking> findByBooker_IdAndStartIsBeforeAndEndIsAfter(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end,
            Sort sort);

    List<Booking> findByBooker_Id(
            Long bookerId,
            Sort sort);

    List<Booking> findByItem_IdAndEndIsBefore(
            Long itemId,
            LocalDateTime end,
            Sort sort);

    List<Booking> findByItem_IdAndStartIsAfter(
            Long itemId,
            LocalDateTime start,
            Sort sort);

    List<Booking> findByItem_Id(
            Long itemId,
            Sort sort);

    List<Booking> findByItem_IdIn(
            List<Long> itemIds,
            Sort sort);

    boolean existsByItem_IdAndBooker_IdAndEndIsBefore(
            Long itemId,
            Long bookerId,
            LocalDateTime end);
}
