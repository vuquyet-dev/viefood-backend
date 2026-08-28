package com.viefood.viefoodidentitysvc.repository;

import com.viefood.viefoodidentitysvc.dto.res.UserRes;
import com.viefood.viefoodidentitysvc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
