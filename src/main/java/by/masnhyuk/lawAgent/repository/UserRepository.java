package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
