package me.melkx.routeplanner.module.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @Query("SELECT COUNT(u.email) > 0 FROM UserEntity u WHERE u.email = :email")
    boolean existsEmail(@Param("email") String email);

    @Query("SELECT u.password_hash FROM UserEntity u WHERE u.email = :email")
    Optional<UserEntity> findByEmail(@Param("email") String email);
}
