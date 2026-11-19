package com.library.repository;

import com.library.entity.BookRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRecommendRepository extends JpaRepository<BookRecommend, String> {

    List<BookRecommend> findByStatusOrderByCreateTimeDesc(String status);

    List<BookRecommend> findByUser_IdOrderByCreateTimeDesc(String userId);
}