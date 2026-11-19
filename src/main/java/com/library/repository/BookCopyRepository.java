package com.library.repository;

import com.library.entity.BookCopy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, String> {

    List<BookCopy> findByBook_IdOrderByCreateTimeAsc(String bookId);

    Optional<BookCopy> findByIdAndBook_Id(String id, String bookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookCopy> findFirstByBook_IdAndIsBorrowedFalseOrderByCreateTimeAsc(String bookId);

    long countByBook_Id(String bookId);

    long countByBook_IdAndIsBorrowedTrue(String bookId);

    @Query("select count(c) from BookCopy c where c.book.id = :bookId and c.isBorrowed = true")
    long countBorrowed(@Param("bookId") String bookId);
}