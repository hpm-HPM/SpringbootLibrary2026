package com.library.repository;

import com.library.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, String>, JpaSpecificationExecutor<Loan> {

    @Query("select count(l) from Loan l where l.user.id = :userId and l.returnDate is null and l.status <> '已还'")
    long countActiveByUser(@Param("userId") String userId);

    Optional<Loan> findFirstByBookCopy_IdAndReturnDateIsNullAndStatusNotOrderByBorrowDateDesc(
            String copyId, String status);

    boolean existsByBookCopy_Id(String copyId);

    long countByBookCopy_Id(String copyId);
}