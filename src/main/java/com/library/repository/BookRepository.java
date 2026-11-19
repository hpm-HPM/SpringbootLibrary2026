package com.library.repository;

import com.library.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {

    @Query("select distinct b.type from Book b where b.type is not null and b.type <> '' order by b.type")
    List<String> findDistinctTypes();

    List<Book> findByTitleContainingIgnoreCaseOrderByCreateTimeDesc(String title);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findForUpdate(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.stock = b.stock + :delta where b.id = :id and b.stock + :delta >= 0")
    int adjustStock(@Param("id") String id, @Param("delta") int delta);
}