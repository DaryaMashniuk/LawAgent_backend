package by.masnhyuk.lawAgent;

import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.repository.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@SpringBootTest
class EmailTest {

    @Autowired
    private DocumentVersionRepository versionRepo;

    @Autowired
    private SubscriptionService subscriptionService;

    @Test
    void testNotification() {
        UUID id = UUID.fromString("feb2cfca-a5d3-4c7d-a7a3-343153fe88dd");
        DocumentVersion version = versionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DocumentVersion not found"));
        subscriptionService.notifySubscribers(version);
    }
}
