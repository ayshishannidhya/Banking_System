package com.asp.authservice.repository;

import com.asp.authservice.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepo extends JpaRepository<Users, Long> {

    @Query("SELECT u FROM Users u WHERE u.username = ?1")
    Optional<Users> findByUsername(String username);
}
