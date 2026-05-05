package it.generation.suonacongigi.dto.event;

import lombok.*;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

// Questo DTO rappresenta la risposta contenente i dati di un evento. 
// Contiene i campi necessari per definire un evento, 
// con le relative informazioni aggiuntive come il numero di posti prenotati, disponibili
// e se l'utente corrente è registrato all'evento.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    // Il campo "id" rappresenta l'identificativo univoco dell'evento.
    @Schema(description = "ID univoco dell'evento", example = "42")
    private Long id;

    // Il campo "title" rappresenta il titolo dell'evento.
    @Schema(description = "Titolo dell'evento", example = "Concerto di Capodanno")
    private String title;

    // Il campo "description" rappresenta la descrizione dell'evento.
    @Schema(description = "Descrizione dell'evento", example = "Un concerto imperdibile per festeggiare l'arrivo del nuovo anno.")
    private String description;

    // Il campo "eventDate" rappresenta la data e ora dell'evento.
    @Schema(description = "Data e ora dell'evento", example = "2026-12-31T20:00:00")
    private LocalDateTime eventDate;

    // Il campo "location" rappresenta la posizione dell'evento.
    @Schema(description = "Posizione dell'evento", example = "Teatro alla Scala, Milano")
    private String location;

    // Il campo "maxSeats" rappresenta il numero massimo di posti disponibili per l'evento.
    @Schema(description = "Numero massimo di posti disponibili per l'evento", example = "100")
    private Integer maxSeats;

    // Il campo "seatsBooked" rappresenta il numero di posti già prenotati per l'evento.
    @Schema(description = "Numero di posti già prenotati per l'evento", example = "50")
    private long seatsBooked;

    // Il campo "seatsAvailable" rappresenta il numero di posti disponibili per l'evento.
    @Schema(description = "Numero di posti disponibili per l'evento", example = "50")
    private long seatsAvailable;

    // Il campo "createdBy" rappresenta l'utente che ha creato l'evento.
    @Schema
    private String createdBy;

    // Il campo "registeredByCurrentUser" indica se l'utente corrente è registrato all'evento.
    @Schema(description = "Indica se l'utente corrente è registrato all'evento", example = "true")
    private boolean registeredByCurrentUser;
}