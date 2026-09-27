package me.melkx.routeplanner.module.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT COUNT(u.email) > 0 FROM UserEntity u WHERE u.email = :email")
    boolean containsEmail(@Param("email") String email);

    @Query("SELECT u.id, u.passwordHash FROM UserEntity u WHERE u.email = :email")
    Optional<UserIdPassword> findIdAndPasswordHashByEmail(@Param("email") String email);
}