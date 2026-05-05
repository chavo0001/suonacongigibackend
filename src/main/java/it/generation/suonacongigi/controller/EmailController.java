package it.generation.suonacongigi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.dto.emailTemplate.EmailRequest;
import it.generation.suonacongigi.dto.emailTemplate.EmailResponse;
import it.generation.suonacongigi.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor

public class EmailController extends BaseController{
    
    private final EmailService emailService;


     @Operation(summary = "Template email", description = "Restituisce il template da modificare.")
    @ApiResponses ({
        @ApiResponse(responseCode = "200", description = "Lista recuperata con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno prova del server")
    })
    @GetMapping("/verification")
    public ResponseEntity<ApiEnvelope<EmailResponse>> getByName() {
        EmailResponse data = emailService.findByName();

        return ok(data, "Template recuperato con successo");
    }



    @Operation(summary = "Aggiorna template (ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Template aggiornato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati di aggiornamento non validi"),
        @ApiResponse(responseCode = "401", description = "Token non valido o assente"),
        @ApiResponse(responseCode = "403", description = "Accesso negato: solo amministratori possono eseguire questa operazione"),
        @ApiResponse(responseCode = "404", description = "Template non trovato")
    })
    @PutMapping("/verification")
    public ResponseEntity<ApiEnvelope<EmailResponse>> update(@Valid @RequestBody @NonNull EmailRequest req) {
        
        EmailResponse data = emailService.update(req);
        
        return ok(data, "Template aggiornato con successo");
    }


}
