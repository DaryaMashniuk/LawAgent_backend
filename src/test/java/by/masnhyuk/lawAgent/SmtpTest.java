package by.masnhyuk.lawAgent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class SmtpTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void testSmtpConnection() {
        mailSender.createMimeMessage();

    }
}