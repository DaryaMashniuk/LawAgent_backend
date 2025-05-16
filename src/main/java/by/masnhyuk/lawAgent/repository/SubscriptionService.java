package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.*;
import by.masnhyuk.lawAgent.util.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {
    private final UserSubscriptionRepository subscriptionRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private static final Logger log = LogManager.getLogger();

    public void subscribeUser(Long userId, DocumentCategory category) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (subscriptionRepository.existsByUserAndCategory(user, category)) {
            throw new IllegalArgumentException("User already subscribed to this category");
        }

        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setCategory(category);
        subscriptionRepository.save(subscription);
    }

    public void unsubscribeUser(Long userId, DocumentCategory category) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Стандартный подход без preview API
        UserSubscription subscription = subscriptionRepository
                .findByUserAndCategory(user, category)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);
    }
    @Transactional
    public void notifySubscribers(DocumentVersion documentVersion) {
        try {
            DocumentEntity document = documentVersion.getDocument();

            if (document == null) {
                log.error("DocumentVersion {} has no associated DocumentEntity", documentVersion.getId());
                return;
            }
            var groupCategory = document.getGroupCategory();
            log.info(groupCategory);
            List<UserSubscription> subscriptions = subscriptionRepository
                    .findByCategory(document.getGroupCategory());

            if (subscriptions.isEmpty()) {
                log.info("No active subscriptions for category {}", document.getGroupCategory());
                return;
            }

            log.info("Sending notifications to {} subscribers", subscriptions.size());

            subscriptions.forEach(subscription -> {
                if (!subscription.getIsActive()) {
                    return;
                }

                Users user = subscription.getUser();
                try {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("userName", user.getUsername());
                    variables.put("subject", "Новая версия документа: " + document.getTitle());
                    variables.put("category", document.getGroupCategory().name());
                    variables.put("documentTitle", document.getTitle());
                    variables.put("documentDescription", documentVersion.getDetails());
                    variables.put("documentLink", generateDocumentVersionLink(documentVersion.getId()));
                    variables.put("unsubscribeLink", generateUnsubscribeLink(user.getId()));
                    log.info("//////////////////////////////////////////////////////////////////////////////");
                    emailService.sendHtmlEmail(
                            user.getEmail(),
                            "Новый документ в вашей подписке",
                            "email/new-document-version-notification",
                            variables
                    );
                    log.info("Notification sent to {}", user.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send email to {}", user.getEmail(), e);
                }
            });
        } catch (Exception e) {
            log.error("Error in notifySubscribers", e);
        }
    }

    private String generateDocumentVersionLink(UUID versionId) {
        return "http://localhost:3000/documents/versions/" + versionId;
    }

    private String generateUnsubscribeLink(Long userId) {
        return "https://lawagent.by/unsubscribe?userId=" + userId;
    }
}