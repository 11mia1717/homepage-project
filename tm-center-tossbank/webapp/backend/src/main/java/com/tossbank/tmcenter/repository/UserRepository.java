package com.tossbank.tmcenter.repository;

import com.tossbank.tmcenter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByEnabledTrue();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.role = 'AGENT' AND u.enabled = true " +
           "ORDER BY (SELECT COUNT(t) FROM TmTarget t WHERE t.assignedAgent = u AND t.status IN ('ASSIGNED', 'IN_CALL')) ASC")
    List<User> findAgentsSortedByWorkload();
}
