package it.generation.suonacongigi.dto.event;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

// Questo DTO rappresenta la richiesta per la creazione o modifica di un evento.

@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {
    // Il campo "title" rappresenta il titolo dell'evento.
    @Schema
    @NotBlank @Size(min = 5, max = 100)
    private String title;

    // Il campo "description" rappresenta la descrizione dell'evento.
    @Schema
    private String description;

    // Il campo "eventDate" rappresenta la data e ora dell'evento. 
    // Deve essere una data futura.
    @Schema(description = "Data e ora dell'evento (deve essere una data futura)", example = "2026-12-31T20:00:00")
    @NotNull @Future
    private LocalDateTime eventDate;

    // Il campo "location" rappresenta la posizione dell'evento.
    @Schema(description = "Posizione dell'evento", example = "Teatro alla Scala, Milano")
    @NotBlank
    private String location;

    // Il campo "maxSeats" rappresenta il numero massimo di posti disponibili per l'evento.
    @Schema(description = "Numero massimo di posti disponibili per l'evento", example = "100")
    @NotNull @Positive
    private Integer maxSeats;
}

