package it.generation.suonacongigi.controller;

import it.generation.suonacongigi.dto.common.ApiEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestControllerAdvice
@Slf4j // Per il logging professionale
public class GlobalExceptionHandler {

    /**
     * 1. ERRORI DI VALIDAZIONE (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleValidation(MethodArgumentNotValidException ex) {
       Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = (err instanceof FieldError) ? ((FieldError) err).getField() : err.getObjectName();
            // Fallback se il messaggio di default dovesse essere nullo
            String message = err.getDefaultMessage() != null ? err.getDefaultMessage() : "Valore non valido";
            errors.put(field, message);
        });

        return ResponseEntity.badRequest().body(
            ApiEnvelope.<Void>builder()
                .success(false)
                .message("Dati non validi: controlla i campi")
                .errors(errors)
                .build()
        );
    }

    /**
     * 2. JSON MALFORMATO O BODY MANCANTE
     * Questo evita il 500 quando la request è incompleta o il JSON è scritto male.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleJsonError(HttpMessageNotReadableException ex) {
        log.warn("Chiamata con JSON malformato o mancante: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiEnvelope.error("La richiesta JSON è incompleta o contiene errori di sintassi"));
    }

    /**
     * 3. TYPE MISMATCH (es: invio "abc" dove il server aspetta un ID Long)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // Recuperiamo il nome del tipo in modo sicuro
        String requiredType = ex.getRequiredType() != null 
                              ? Objects.requireNonNull(ex.getRequiredType()).getSimpleName() 
                              : "formato atteso";

        String msg = String.format("Il parametro '%s' deve essere di tipo %s", 
                     ex.getName(), requiredType);
        
        log.warn("Mismatch di tipo nell'URL: parametro {}, atteso {}", ex.getName(), requiredType);
        
        return ResponseEntity.badRequest().body(ApiEnvelope.error(msg));
    }

    /**
     * 4. VIOLAZIONE INTEGRITÀ DATABASE (es: Duplicate Key su email)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleDatabaseConflict(DataIntegrityViolationException ex) {
        log.error("Violazione integrità DB: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiEnvelope.error("Operazione non riuscita: possibile duplicazione dati o vincoli violati"));
    }

    /**
     * 5. RISORSA NON TROVATA
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiEnvelope.error(ex.getMessage()));
    }

    /**
     * 6. ERRORI DI LOGICA BUSINESS (Lanciati manualmente con IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleBusinessLogic(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiEnvelope.error(ex.getMessage()));
    }

    /**
     * 7. STATO ILLEGALE (es: Posti esauriti)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiEnvelope.error(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleAuth(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiEnvelope.error("Credenziali non valide"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiEnvelope.error("Accesso non autorizzato"));
    }

    /**
     * 8. IL "PARACADUTE" FINALE
     * Qui finiscono solo i veri errori imprevisti del codice.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiEnvelope<Void>> handleGeneric(Exception ex) {
        log.error("ERRORE INTERNO CRITICO: ", ex); // Logghiamo lo stacktrace per noi
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiEnvelope.error("Errore imprevisto. I nostri tecnici sono stati avvisati."));
    }
}