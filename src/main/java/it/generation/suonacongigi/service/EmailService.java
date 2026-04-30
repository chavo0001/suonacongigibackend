package it.generation.suonacongigi.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.model.VerificationToken;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${app.base-url}") 
    private String baseUrl;

public void sendVerificationMail(User user, VerificationToken token){
    String link = baseUrl + "/auth/verify?token=" + token.getToken();
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(user.getEmail());
    message.setSubject("Conferma la tua mail");
    message.setText("Clicca sul seguente link per completare la tua registrazione: \n" + link + "\n Hai 24 ore per completare la verifica!");

    mailSender.send(message);
    
    System.out.println("mail mandata con successo \n");
   


    



}    


}
