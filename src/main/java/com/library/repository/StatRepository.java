package com.library.repository;

import com.library.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StatRepository extends JpaRepository<Loan, String> {

    @Query(value = """
            SELECT COALESCE(NULLIF(b.type, ''), '未分类') AS name, COUNT(l.id) AS cnt
            FROM book b
            LEFT JOIN bookcopy c ON c.bookId = b.id
            LEFT JOIN loan l ON l.bookCopyId = c.id
            GROUP BY COALESCE(NULLIF(b.type, ''), '未分类')
            ORDER BY cnt DESC, name ASC
            """, nativeQuery = true)
    List<Object[]> countByBookType();

    @Query(value = """
            SELECT COALESCE(NULLIF(u.major, ''), '未填写') AS name, COUNT(l.id) AS cnt
            FROM `user` u
            LEFT JOIN loan l ON l.userId = u.id
            GROUP BY COALESCE(NULLIF(u.major, ''), '未填写')
            ORDER BY cnt DESC, name ASC
            """, nativeQuery = true)
    List<Object[]> countByUserMajor();
}