package com.library.repository;

import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByName(String name);

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    @Query("select distinct u.major from User u where u.major is not null and u.major <> '' order by u.major")
    List<String> findDistinctMajors();
}