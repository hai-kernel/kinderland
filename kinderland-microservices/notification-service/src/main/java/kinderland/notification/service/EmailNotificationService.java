package kinderland.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import kinderland.notification.event.UserCreatedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(UserCreatedEvent event) {
        String fullName = ((safe(event.getFirstName()) + " " + safe(event.getLastName())).trim());
        if (fullName.isBlank()) {
            fullName = event.getUsername();
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getEmail());
            message.setSubject("Chào mừng đến với Kinderland!");
            message.setText("""
                    Xin chào %s,

                    Tài khoản của bạn đã được tạo thành công tại Kinderland.
                    Chúc bạn mua sắm vui vẻ!

                    — Kinderland Team
                    """.formatted(fullName));

            mailSender.send(message);
            log.info("Đã gửi email chào mừng tới {}", event.getEmail());
        } catch (Exception e) {
            // Không ném lại để Kafka không retry vô hạn vì lỗi gửi mail.
            log.error("Gửi email chào mừng THẤT BẠI tới {}: {}", event.getEmail(), e.getMessage());
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
