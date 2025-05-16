package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.UserSubscription;
import by.masnhyuk.lawAgent.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByCategory(DocumentCategory category);

    boolean existsByUserAndCategory(Users user, DocumentCategory category);
    Optional<UserSubscription> findByUserAndCategory(Users user, DocumentCategory category);
}