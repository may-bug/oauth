package org.codelin.oauth.oauth.identity.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.SmtpConfig;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.SmtpConfigMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

/**
 * 邮件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements NotificationService {

    private final SmtpConfigMapper smtpConfigMapper;

    @Override
    public void sendEmail(String to, String subject, String content) {
        SmtpConfig config = getSmtpConfig();
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            log.warn("SMTP not configured or disabled");
            throw BizException.badRequest("error.email.smtp-not-configured");
        }

        try {
            JavaMailSender mailSender = createMailSender(config);

            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(config.getFromAddress(), config.getFromName()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, "UTF-8");
            message.setContent(content, "text/html; charset=UTF-8");

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw BizException.badRequest("error.email.send-failed");
        }
    }

    @Override
    public void sendSms(String phone, String content) {
        throw new UnsupportedOperationException("短信发送暂未实现");
    }

    @Override
    public void sendEmailByTemplate(String to, String templateCode, java.util.Map<String, String> variables) {
        throw new UnsupportedOperationException("模板邮件发送暂未实现");
    }

    /**
     * 获取SMTP配置
     */
    private SmtpConfig getSmtpConfig() {
        List<SmtpConfig> list = smtpConfigMapper.selectAll();
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 创建邮件发送器
     */
    private JavaMailSender createMailSender(SmtpConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if (Boolean.TRUE.equals(config.getSslEnabled())) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }
}
