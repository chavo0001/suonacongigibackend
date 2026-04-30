package it.generation.suonacongigi.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ARCHITETTURA: Unified API Envelope.
 * Questa classe definisce il contratto unico per ogni risposta del server.
 * @JsonInclude(JsonInclude.Include.NON_NULL): istruisce Jackson a non inviare chiavi vuote,
 * risparmiando banda e pulendo il JSON per il frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Involucro universale per tutte le risposte API")
public class ApiEnvelope<T> {

    @Schema(description = "Indica se l'operazione è andata a buon fine")
    private boolean success;

    @Schema(description = "Messaggio testuale per l'utente o il log")
    private String message;

    @Schema(description = "Payload della risposta (presente solo in caso di successo)")
    private T data;

    @Schema(description = "Dettaglio degli errori (presente solo in caso di fallimento validazione)")
    private Map<String, String> errors;

    @Schema(description = "Timestamp ISO-8601 della risposta")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // --- Static Factory Methods per velocizzare i controller ---

    public static <T> ApiEnvelope<T> ok(T data, String message) {
        return ApiEnvelope.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiEnvelope<T> error(String message) {
        return ApiEnvelope.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}