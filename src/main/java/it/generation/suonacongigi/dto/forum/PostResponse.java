package it.generation.suonacongigi.dto.forum;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la risposta contenente i dati di un post all'interno di un thread del forum, 
// con le relative informazioni aggiuntive come il nome dell'autore, la data di creazione 
// e se l'utente corrente ha i permessi per modificare il post.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    // Il campo "id" rappresenta l'identificativo univoco del post.
    @Schema(description = "ID univoco del post", example = "1")
    private Long id;

    // Il campo "content" rappresenta il contenuto del post.
    @Schema(description = "Contenuto del post", example = "Questo è il contenuto del post.")
    private String content;

    // Il campo "authorName" rappresenta il nome dell'autore del post.
    @Schema(description = "Nome dell'autore del post", example = "Mario Rossi")
    private String authorName;

    // Il campo "createdAt" rappresenta la data di creazione del post.
    @Schema(description = "Data di creazione del post", example = "2023-01-01T12:00:00")        
    private LocalDateTime createdAt;

    // Il campo "canEdit" indica se l'utente corrente ha i permessi per modificare il post.
    @Schema(description = "Indica se l'utente corrente ha i permessi per modificare il post", example = "true")
    private boolean canEdit;
}
