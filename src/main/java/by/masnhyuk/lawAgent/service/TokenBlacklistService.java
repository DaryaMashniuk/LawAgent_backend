package by.masnhyuk.lawAgent.service;

public interface TokenBlacklistService {
    void addToBlacklist(String token);
    boolean isTokenBlacklisted(String token);
}