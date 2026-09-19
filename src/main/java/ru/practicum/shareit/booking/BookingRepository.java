package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
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

    List<Booking> findByItem_IdInAndEndIsBefore(
            List<Long> itemIds,
            LocalDateTime end,
            Sort sort);

    List<Booking> findByItem_IdInAndStartIsAfter(
            List<Long> itemIds,
            LocalDateTime start,
            Sort sort);

    List<Booking> findByItem_IdInAndStartIsBeforeAndEndIsAfter(
            List<Long> itemIds,
            LocalDateTime start,
            LocalDateTime end,
            Sort sort);

    List<Booking> findByItem_IdInAndStatus(
            List<Long> itemIds,
            BookingStatus status,
            Sort sort);

    boolean existsByItem_IdAndBooker_IdAndEndIsBefore(
            Long itemId,
            Long bookerId,
            LocalDateTime end);

    List<Booking> findByItem_IdInAndStatus(
            Collection<Long> itemIds,
            BookingStatus status,
            Sort sort);
}