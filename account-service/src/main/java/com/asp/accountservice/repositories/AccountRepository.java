package com.asp.accountservice.repositories;

import com.asp.accountservice.models.Account;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(String candidate);

    Account getAccountByAccountId(Long accountId);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("delete from Account a where a.accountId = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);
}
