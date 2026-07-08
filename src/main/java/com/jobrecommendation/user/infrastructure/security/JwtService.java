package com.jobrecommendation.user.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.secret:jobplatform-super-secret-key-which-should-be-changeable-and-sufficiently-secure}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // Default 24 hours in milliseconds
    private long expirationMs;

    public String generateToken(String email) {
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(Algorithm.HMAC256(secret));
    }

    public String extractEmail(String token) {
        DecodedJWT decodedJWT = decodeToken(token);
        return decodedJWT != null ? decodedJWT.getSubject() : null;
    }

    public boolean validateToken(String token) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT != null && !isTokenExpired(decodedJWT);
        } catch (Exception e) {
            return false;
        }
    }

    private DecodedJWT decodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTokenExpired(DecodedJWT decodedJWT) {
        return decodedJWT.getExpiresAt().before(new Date());
    }
}
