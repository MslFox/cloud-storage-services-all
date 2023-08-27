package com.mslfox.cloudStorageServices.security;

import com.mslfox.cloudStorageServices.entities.auth.UserAuthority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Data
@ConfigurationProperties("security.jwt")
public class JwtProvider {
    private String secret;
    private Long expirationInHours;
    private String authoritiesClaimName;

    public String generateJwt(UserDetails userDetails) {
        String username = userDetails.getUsername();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInHours * 3_600_000);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .claim(authoritiesClaimName, userDetails.getAuthorities())
                .compact();
    }

    public boolean validateJwt(String jwt)  {
        Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt);
        return true;
    }

    public Claims getClaims(String jwt) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getBody();
    }

    public Set<UserAuthority> getAuthorities(String jwt) {
        var claims = getClaims(jwt);
        List<String> authorities = claims.get(this.authoritiesClaimName, List.class);
        return authorities.stream().map(UserAuthority::valueOf).collect(Collectors.toSet());
    }

    public String getUsername(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(jwt)
                .getBody();
        return claims.getSubject();
    }
}