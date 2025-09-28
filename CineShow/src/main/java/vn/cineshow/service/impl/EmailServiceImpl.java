package vn.cineshow.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailServiceImpl {

    private final SendGrid sendgrid;
    private final String TEMPLATE_ID = "d-71a24bbc41824c0495bc166a115275a0";
    @Value("${spring.sendgrid.from-email}")
    private String from;
    @Value("${app.frontend.verify-url}")
    private String verifyBaseUrl;
    @Value("${app.sendgrid.otp-template-id}")
    private String otpTemplateId;

    /**
     * Send email by SendGrid
     *
     * @param to send email to someone
     * @param subject
     * @param text
     */
    public void send(String to, String subject, String text) {
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        Content content = new Content("text/plain", text);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        sendRequest(mail, "Send Email", to);
    }

    /**
     * Gửi OTP email bằng SendGrid template (có kèm tên người nhận)
     *
     * @param to   email người nhận
     * @param name tên hiển thị trong email
     * @param otp  mã OTP
     */
    public void sendOtpEmail(String to, String name, String otp, String subject) {
        Email from = new Email(this.from);
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.setTemplateId(otpTemplateId);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(to));
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("otp", otp);

        personalization.addDynamicTemplateData("verifyUrl", verifyBaseUrl + "?email=" + to);

        mail.addPersonalization(personalization);

        sendRequest(mail, "OTP Email", to);
    }

    /**
     * Helper gọi SendGrid API + log kết quả
     */
    private void sendRequest(Mail mail, String type, String to) {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendgrid.api(request);
            if (response.getStatusCode() == 202) {
                log.info("{} sent successfully to {}", type, to);
            } else {
                log.error("{} failed to {}. Response: {}", type, to, response.getBody());
            }
        } catch (IOException e) {
            log.error("Error sending {} to {}: {}", type, to, e.getMessage());
        }
    }
}