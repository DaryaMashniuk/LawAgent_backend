package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.entity.Users;
import by.masnhyuk.lawAgent.entity.MyUserPrincipal;
import by.masnhyuk.lawAgent.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LogManager.getLogger();
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username);
        log.info("User found: {}", user);

        if(user == null) {
            log.error("User not found for username: {}", username);
            throw new UsernameNotFoundException("User not found for username: " + username);
        }
        return new MyUserPrincipal(user);
    }
}
