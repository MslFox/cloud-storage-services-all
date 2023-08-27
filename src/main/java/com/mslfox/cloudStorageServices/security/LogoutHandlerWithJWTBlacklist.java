package com.mslfox.cloudStorageServices.security;

import com.mslfox.cloudStorageServices.entities.jwt.BlackJwtEntity;
import com.mslfox.cloudStorageServices.repository.jwt.BlackJwtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutHandlerWithJWTBlacklist implements LogoutSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtFilter jwtFilter;
    private final BlackJwtRepository blackJwtRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        try {
            var jwt = jwtFilter.getJwt(httpServletRequest);
            if (jwtProvider.validateJwt(jwt)) {
                blackJwtRepository.save(BlackJwtEntity.builder()
                        .token(jwt)
                        .expiration(jwtProvider.getClaims(jwt).getExpiration().getTime())
                        .build());
            }
            new Thread(() -> blackJwtRepository.deleteByExpirationBefore(new Date().getTime())).start();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
