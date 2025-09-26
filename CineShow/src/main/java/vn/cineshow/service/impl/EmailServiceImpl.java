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

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendgrid.api(request);
            if (response.getStatusCode() == 202) { //accepted
                log.info("Email Sent Successfully");
            } else {
                log.error("Email Sent Failed" + response.getBody());
            }
        } catch (IOException e) {
            log.error("Error occurred while sending email, error message: " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String to, String name, String verificationUrl, String homeUrl) {
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setTemplateId(TEMPLATE_ID);
        mail.setSubject("Verification Email");

        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("verification_url", verificationUrl);
        personalization.addDynamicTemplateData("home_url", homeUrl);
        mail.addPersonalization(personalization);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendgrid.api(request);
            if (response.getStatusCode() == 202) { //accepted
                log.info("Email Sent Successfully");
            } else {
                log.error("Email Sent Failed" + response.getBody());
            }
        } catch (IOException e) {
            log.error("Error occurred while sending email, error message: " + e.getMessage());
        }
    }

}
