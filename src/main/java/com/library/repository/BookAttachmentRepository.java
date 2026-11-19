package com.library.repository;

import com.library.entity.BookAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookAttachmentRepository extends JpaRepository<BookAttachment, String> {

    List<BookAttachment> findByBookIdOrderByIsCoverDescSortOrderAscCreateTimeAsc(String bookId);

    Optional<BookAttachment> findFirstByBookIdAndIsCoverTrue(String bookId);

    List<BookAttachment> findByBookIdInAndIsCoverTrue(Collection<String> bookIds);

    long countByBookId(String bookId);
}