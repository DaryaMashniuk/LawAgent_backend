package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTServiceImpl implements by.masnhyuk.lawAgent.service.JWTService {

    private String secretKey="";
    private final long jwtExpiration = 86400000;
    private final TokenBlacklistService tokenBlacklistService;

    public JWTServiceImpl(TokenBlacklistService tokenBlacklistService) {
        try {
            this.tokenBlacklistService = tokenBlacklistService;
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey key = keyGenerator.generateKey();
            secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String extractUserName(String token) {
        return extractClaim(token,Claims::getSubject);
    }


    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Override
    public String generateToken(String username) {
        Map<String,Object> claims = new HashMap<>();

        return Jwts
                .builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .and()
                .signWith(getKey())
                .compact();

    }

}
