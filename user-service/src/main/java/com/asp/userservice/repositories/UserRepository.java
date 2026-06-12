package com.asp.userservice.repositories;

import com.asp.userservice.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    @Query("SELECT u FROM Users u WHERE u.contactDetails.email = ?1")
    Boolean existsByEmail(String email);

    Boolean existsByUserid(Long userId);

    Optional<Users> findByUserid(Long userId);
}
