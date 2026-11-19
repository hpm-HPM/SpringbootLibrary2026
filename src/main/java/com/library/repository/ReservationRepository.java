package com.library.repository;

import com.library.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, String>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByBook_IdAndStatusOrderByCreateTimeAsc(String bookId, String status);

    boolean existsByUser_IdAndBook_IdAndStatusIn(String userId, String bookId, Collection<String> statuses);

    Optional<Reservation> findFirstByUser_IdAndBook_IdAndStatusInOrderByCreateTimeDesc(
            String userId, String bookId, Collection<String> statuses);

    List<Reservation> findByStatusAndExpireTimeBefore(String status, LocalDateTime time);

    long countByStatus(String status);
}