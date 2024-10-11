package com.team04.buy_gurus.email;

import com.team04.buy_gurus.exception.ex_user.ex.CodeExpiredException;
import com.team04.buy_gurus.exception.ex_user.ex.CodeMismatchException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    public void sendVerificationCode(String email) throws MessagingException {

        String verificationCode = generateVerificationCode();
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set(email, verificationCode, 5, TimeUnit.MINUTES);

        sendEmail(email, verificationCode);
    }

    public void sendEmail(String email, String verificationCode) throws MessagingException {
        log.info("이메일 서비스");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setTo(email);
        helper.setSubject("이메일 인증 코드");
        helper.setText("인증 코드: " + verificationCode);

        mailSender.send(message);
    }

    public void verifyCode(String email, String code) {

        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String storedCode = valueOps.get(email);

        if (storedCode == null) {
            throw new CodeExpiredException();
        }

        if (!storedCode.equals(code)) {
            throw new CodeMismatchException();
        }

        valueOps.set(email + ":verified", "true", 10, TimeUnit.MINUTES);
        redisTemplate.delete(email);
    }

    public String generateVerificationCode() {

        int randomCode = (int) (Math.random() * 1000000);
        return String.format("%06d", randomCode);
    }
}
