package com.example.uberprojectauthservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService implements CommandLineRunner {

    @Value( "${jwt.secret}")
    private  String SECRET;

    @Value( "${jwt.expiry}")
    private int expiry;


    /**
     * Creates a JWT token with the given payload and email.
     *
     * @param payload the map containing payload data
     * @param email the subject email
     * @return the generated JWT token
     */
    public String createToken(Map<String, Object> payload,String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry*1000L);
        return Jwts.builder()
                .claims(payload)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryDate).subject(email)
                .signWith(getSigningKey())
                .compact();
    }
    public String createToken(String email) {
        return createToken(Map.of("email",email),email);
    }
    /**
     * Extracts all claims from the given JWT token.
     *
     * @param token the JWT token
     * @return the Claims extracted from the token
     */
    public Claims extractAllPayload(String token){
        return  Jwts
                .parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    /**
     * Extracts a specific claim from the given JWT token using a resolver function.
     *
     * @param token the JWT token
     * @param claimsResolver the function to resolve the claim
     * @param <T> the type of the claim
     * @return the resolved claim
     */
    public  <T> T extractPayload(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllPayload(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the subject email from the given JWT token.
     *
     * @param token the JWT token
     * @return the email extracted from the token
     */
    public String extractEmail(String token) {
        return extractPayload(token,Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token the JWT token
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractPayload(token,Claims::getExpiration);
    }

    /**
     * Checks if the given JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Gets the signing key for the JWT tokens.
     *
     * @return the signing key
     */
    public Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the given JWT token against the provided email.
     *
     * @param token the JWT token
     * @param email the email to validate against
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token, String email) {
        return extractEmail(token).equals(email) && !isTokenExpired(token);
    }

    /**
     * Extracts the phone number from the given JWT token.
     *
     * @param token the JWT token
     * @return the phone number extracted from the token
     */
    public String extractPhoneNumber(String token) {
        return extractPayload(token,claims -> claims.get("phoneNumber").toString());
    }

    /**
     * Runs the demonstration of token generation and extraction.
     *
     * @param args the command-line arguments
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void run(String... args) throws Exception {
//        String token = createToken(Map.of("email","Nasir@b","phoneNumber","99999999"),"Nasir");
//        System.out.println("Generated token is :"+token);
//        System.out.println("extracted email is :"+extractEmail(token));
//        System.out.println("extracted phoneNumber is :"+extractPhoneNumber(token));
//        System.out.println("is token expired :"+isTokenExpired(token));
//        System.out.println("is token valid :"+validateToken(token,"Nasir"));
    }
}
