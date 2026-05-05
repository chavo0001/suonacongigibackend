package it.generation.suonacongigi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import it.generation.suonacongigi.dto.emailTemplate.EmailRequest;
import it.generation.suonacongigi.dto.emailTemplate.EmailResponse;
import it.generation.suonacongigi.model.EmailTemplate;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.model.VerificationToken;
import it.generation.suonacongigi.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final EmailTemplateRepository emailTemplateRepository;
    private final JavaMailSender mailSender;
    
    @Value("${app.base-url}") 
    private String baseUrl;

    public void sendVerificationMail(User user, VerificationToken token) {
        String link = baseUrl + "verify?token=" + token.getToken();
        
        try {
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            EmailTemplate emailTemplate = emailTemplateRepository.findByName("verification")
                        .orElseThrow(() -> new RuntimeException("Template not found"));

            helper.setFrom(Objects.requireNonNull(emailTemplate.getSender()));
            helper.setTo(Objects.requireNonNull(user.getEmail()));
            helper.setSubject(Objects.requireNonNull(emailTemplate.getSubject()));

            // 1. load template (DB or file)
            String template = emailTemplate.getContent();

            // 2. prepare variables
            Context context = new Context();
            context.setVariable("link", link);


            // 3. process template into final HTML
            String html = template.replace("{{link}}", link);

            // 4. inject HTML into email
            helper.setText(Objects.requireNonNull(html), true);

            mailSender.send(message);

            // System.out.println("mail mandata con successo\n");

        } catch (MessagingException e) {
            throw new RuntimeException("Errore durante l'invio della mail", e);
        }
    } 

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
        public EmailResponse findByName() {
            System.out.println("recupero il template");
            EmailTemplate emailTemplate = emailTemplateRepository.findByName("verification")
                .orElseThrow(() -> new RuntimeException("Template not found"));
                
            return toResponse(emailTemplate);
        }




     private EmailResponse toResponse(EmailTemplate emailTemplate) {
        

        return Objects.requireNonNull(EmailResponse.builder()
                .sender(emailTemplate.getSender())
                .subject(emailTemplate.getSubject())
                .content(emailTemplate.getContent())
                .build());
    }   


    @Transactional
    public EmailResponse update(EmailRequest req){
        EmailTemplate emailTemplate = emailTemplateRepository.findByName("verification")
                .orElseThrow(() -> new RuntimeException("Template not found"));

        emailTemplate.setSender(Objects.requireNonNull(req.getSender()));
        emailTemplate.setSubject(Objects.requireNonNull(req.getSubject()));
        emailTemplate.setContent(Objects.requireNonNull(req.getContent()));

        EmailTemplate save = Objects.requireNonNull(emailTemplateRepository.save(emailTemplate));
        
        return toResponse(save);
    }


}
