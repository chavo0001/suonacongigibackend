package it.generation.suonacongigi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.dto.forum.*;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.service.ForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

// Questo controller gestisce le operazioni relative al forum, come la consultazione delle categorie, 
// la visualizzazione dei thread, la creazione di nuove discussioni e l'aggiunta di post. 
// Alcuni endpoint richiedono autenticazione, mentre altri sono pubblici.
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@Tag(name = "Forum", description = "Community & Discussion Engine")
public class ForumController extends BaseController {

    // Iniezione del servizio che contiene la logica di business per il forum.
    private final ForumService forumService;

    // Endpoint pubblico per ottenere la lista delle categorie del forum. 
    // Restituisce un elenco di categorie con i relativi ID e nomi.
    @Operation(summary = "Lista categorie")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista categorie recuperata con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/categories")
    public ResponseEntity<ApiEnvelope<List<CategoryResponse>>> getCategories() {
        // Il controller si limita a delegare la logica di recupero delle categorie al servizio, 
        // restituendo un ResponseEntity con il risultato.
        List<CategoryResponse> data = forumService.getCategories();

        return ok(data, "Lista categorie recuperata con successo");
    }

    // Endpoint pubblico per ottenere la lista dei thread appartenenti a una specifica categoria tramite ID.
    @Operation(summary = "Thread per categoria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista thread recuperata con successo"),
        @ApiResponse(responseCode = "404", description = "Categoria non trovata"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/categories/{id}/threads")
    public ResponseEntity<ApiEnvelope<List<ThreadSummaryResponse>>> getThreads(@PathVariable Long id) {
        // Il controller si limita a delegare la logica di recupero dei thread al servizio, 
        // restituendo un ResponseEntity con il risultato.
        List<ThreadSummaryResponse> data = forumService.getThreadsByCategory(Objects.requireNonNull(id));

        return ok(data, "Lista thread recuperata con successo");
    }

    // Endpoint pubblico per ottenere i dettagli di un thread specifico tramite ID.
    @Operation(summary = "Dettaglio thread")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dettaglio thread recuperato con successo"),
        @ApiResponse(responseCode = "404", description = "Thread non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    }) 
    @GetMapping("/threads/{id}")
    public ResponseEntity<ApiEnvelope<ThreadDetailResponse>> getThread(
        @Parameter(description = "ID del thread da recuperare", example = "42")
        @PathVariable Long id, 
        @AuthenticationPrincipal User user) {
            // Se l'utente è autenticato, passiamo il suo username al servizio per verificare i permessi.
            String username = (user != null) ? user.getUsername() : null;

            // Il controller si limita a delegare la logica di recupero dei dettagli del thread al servizio, 
            // restituendo un ResponseEntity con il risultato.
            ThreadDetailResponse data = forumService.getThreadDetail(Objects.requireNonNull(id), username);

            return ok(data, "Dettaglio thread recuperato con successo");
    }

    // Endpoint protetto per creare una nuova discussione (thread) in una categoria specifica. 
    // Accettando un DTO con i dati del thread, valida i dati e delega la logica al servizio. 
    // Restituisce i dettagli del thread creato in caso di successo. 
    // Accessibile solo agli utenti autenticati.
    @Operation(summary = "Nuova discussione")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Discussione creata con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/threads")
    public ResponseEntity<ApiEnvelope<ThreadSummaryResponse>> createThread(
        @Valid @RequestBody ThreadRequest req,
        @AuthenticationPrincipal User user) {
            // @Valid: Innesca il motore di validazione Bean Validation via Reflection sui metadati del DTO.
            String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());
            // Il controller si limita a delegare la logica di creazione del thread al servizio,
            // restituendo un ResponseEntity con il risultato. 
            // La validazione dei dati avviene automaticamente grazie alle annotazioni di validazione sui campi del DTO.
            ThreadSummaryResponse data = forumService.createThread(Objects.requireNonNull(req), username);

            return ok(data, "Discussione creata con successo");
    }

    // Endpoint protetto per rispondere a un thread esistente tramite ID. 
    // Accetta un DTO con i dati del post, valida i dati e delega la logica al servizio. 
    // Restituisce i dettagli del post creato in caso di successo. 
    // Accessibile solo agli utenti autenticati.
    @Operation(summary = "Rispondi")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post aggiunto con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "404", description = "Thread non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/threads/{id}/posts")
    public ResponseEntity<ApiEnvelope<PostResponse>> addPost(
        @Parameter(description = "ID del thread a cui rispondere", example = "42")
        @PathVariable Long id,
        @Valid @RequestBody PostRequest req,
        @AuthenticationPrincipal User user) {
            // @Valid: Innesca il motore di validazione Bean Validation via Reflection sui metadati del DTO.
            String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());
            // Il controller si limita a delegare la logica di aggiunta del post al servizio,
            // restituendo un ResponseEntity con il risultato. 
            // La validazione dei dati avviene automaticamente grazie alle annotazioni di validazione sui campi del DTO.
            PostResponse data = forumService.addPost(Objects.requireNonNull(id), Objects.requireNonNull(req), username);

            return ok(data, "Post aggiunto con successo");
    }

    // Endpoint protetto per eliminare un post tramite ID. 
    // Solo l'autore del post o un amministratore possono eliminarlo. 
    // Accessibile solo agli utenti autenticati.
    @Operation(summary = "Elimina post")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post eliminato con successo"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "404", description = "Post non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiEnvelope<Void>> deletePost(
        @Parameter(description = "ID del post da eliminare", example = "123")
        @PathVariable Long id,
        @AuthenticationPrincipal User user) {
        // Solo l'autore del post o un amministratore possono eliminarlo
        Objects.requireNonNull(user);

        // Il servizio si occuperà di verificare i permessi e l'esistenza del post
        forumService.deletePost(
            Objects.requireNonNull(id), 
            Objects.requireNonNull(user.getUsername()), 
            user.getRole() == User.Role.ADMIN);
        
        // Se il servizio non ha lanciato eccezioni, significa che l'eliminazione è avvenuta con successo
        return ok(null, "Post eliminato con successo");
    }
}