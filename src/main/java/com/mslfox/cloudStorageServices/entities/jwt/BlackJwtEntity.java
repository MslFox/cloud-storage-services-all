package com.mslfox.cloudStorageServices.entities.jwt;

import lombok.*;

import javax.persistence.*;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "black_jwt")
public class BlackJwtEntity {
    private String token;
    @Id
    private Long expiration;

}
