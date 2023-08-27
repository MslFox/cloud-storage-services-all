package com.mslfox.cloudStorageServices.repository.jwt;

import com.mslfox.cloudStorageServices.entities.jwt.BlackJwtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Repository
public interface BlackJwtRepository extends JpaRepository<BlackJwtEntity, Long> {

    @Transactional
    void deleteByExpirationBefore(Long expiration);
    Optional<BlackJwtEntity> findByToken(String token);
}
