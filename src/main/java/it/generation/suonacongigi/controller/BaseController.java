package it.generation.suonacongigi.controller;

import it.generation.suonacongigi.dto.common.ApiEnvelope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * ARCHITETTURA: Base Controller Astratto.
 * Fornisce metodi helper protetti per uniformare le risposte di successo.
 * Centralizza la logica di creazione della ResponseEntity per tutti i controller derivati.
 */
public abstract class BaseController {

    /**
     * Risposta 200 OK con payload.
     */
    protected <T> ResponseEntity<ApiEnvelope<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiEnvelope.ok(data, message));
    }

    /**
     * Risposta 201 CREATED per operazioni di inserimento.
     */
    protected <T> ResponseEntity<ApiEnvelope<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiEnvelope.ok(data, message));
    }

    /**
     * Risposta 200 OK per operazioni di cancellazione o aggiornamenti senza ritorno dati.
     */
    protected ResponseEntity<ApiEnvelope<Void>> deleted(String message) {
        return ResponseEntity.ok(ApiEnvelope.ok(null, message));
    }

    /**
     * Risposta 204 NO CONTENT (usata raramente se preferiamo comunque un messaggio JSON).
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}