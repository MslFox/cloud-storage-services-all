package com.mslfox.cloudStorageServices.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mslfox.cloudStorageServices.config.SecurityConfig;
import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import com.mslfox.cloudStorageServices.exception.BadRequestException;
import com.mslfox.cloudStorageServices.messages.ErrorMessage;
import com.mslfox.cloudStorageServices.model.error.ErrorResponse;
import com.mslfox.cloudStorageServices.repository.jwt.BlackJwtRepository;
import io.jsonwebtoken.SignatureException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final ErrorMessage errorMessage;
    private final BlackJwtRepository blackJwtRepository;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public void doFilterInternal(HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!isPublicUri(httpServletRequest.getRequestURI())) {
            try {
                String jwt = getJwt(httpServletRequest);
                if (StringUtils.hasText(jwt)
                        && jwtProvider.validateJwt(jwt)
                        && !isBlackListed(jwt)) {
                    final var authentication = new UsernamePasswordAuthenticationToken(
                            jwtProvider.getUsername(jwt), null, jwtProvider.getAuthorities(jwt));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                final var objectMapper = new ObjectMapper();
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(e.getMessage(), 1L)));
                return;
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    public String getJwt(HttpServletRequest request) throws SignatureException {
        String authHeader = request.getHeader(HeaderNameHolder.TOKEN_HEADER_NAME);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        } else throw new SignatureException(errorMessage.jwtStartBearer());

    }

    public boolean isBlackListed(String jwt) throws BadRequestException {
        if (blackJwtRepository.findByToken(jwt).isPresent()) {
            throw new BadRequestException(errorMessage.getJwtBlacklisted());
        } else {
            return false;
        }
    }

    private boolean isPublicUri(String requestURI) {
        for (String publicUri : SecurityConfig.PUBLIC_URIS) {
            if (antPathMatcher.match(publicUri, requestURI))
                return true;
        }
        return false;
    }
}