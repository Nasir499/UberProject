package com.example.clientsocketservice.configurations;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    @Value("${jwt.secret:default_uber_jwt_secret_key_change_in_production_min_256_bits_12345}")
    private String secret;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            List<String> passcode = accessor.getNativeHeader("passcode");

            String token = null;
            if (authorization != null && !authorization.isEmpty() && authorization.get(0).startsWith("Bearer ")) {
                token = authorization.get(0).substring(7);
            } else if (passcode != null && !passcode.isEmpty()) {
                token = passcode.get(0);
            }

            if (token != null && !token.isBlank()) {
                try {
                    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parser()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                    String userEmail = claims.getSubject();
                    logger.info("STOMP WebSocket client authenticated as: {}", userEmail);
                    accessor.setUser(new java.security.Principal() {
                        @Override
                        public String getName() {
                            return userEmail;
                        }
                    });
                } catch (Exception e) {
                    logger.warn("STOMP WebSocket authentication failed: {}", e.getMessage());
                }
            } else {
                logger.info("STOMP WebSocket client connected (no auth token provided)");
            }
        }
        return message;
    }
}
