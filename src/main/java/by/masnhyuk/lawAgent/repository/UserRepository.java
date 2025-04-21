package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
}
