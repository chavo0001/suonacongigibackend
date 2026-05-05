package it.generation.suonacongigi.controller;

import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.service.EventLikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import it.generation.suonacongigi.dto.event.EventResponse;
import java.util.List;

import java.util.Objects;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventLikeController extends BaseController {

    private final EventLikeService likeService;

    @Operation(summary = "Metti o togli Like", description = "Aggiunge un like se non presente, lo rimuove se già esistente.")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiEnvelope<String>> toggleLike(
            @PathVariable Long id, 
            @AuthenticationPrincipal User user) {
        
        // Recuperiamo l'username dal token JWT proprio come in EventController
        String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());
        
        String message = likeService.toggleLike(Objects.requireNonNull(id), username);
        
        return ok(message, "Operazione Like completata");
    }

    @Operation(summary = "Conta Like", description = "Restituisce il numero totale di like per un evento.")
    @GetMapping("/{id}/likes/count")
    public ResponseEntity<ApiEnvelope<Long>> getLikesCount(@PathVariable Long id) {
        long count = likeService.getLikeCount(Objects.requireNonNull(id));
        return ok(count, "Conteggio like recuperato");
    }

    @Operation(summary = "Miei Like", description = "Restituisce la lista di tutti gli eventi a cui l'utente ha messo like.")
    @GetMapping("/liked")
    public ResponseEntity<ApiEnvelope<List<EventResponse>>> getMyLikedEvents(
            @AuthenticationPrincipal User user) {
        
        // Estraiamo l'username dell'utente loggato dal token
        String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());
        
        // Chiamiamo il metodo che abbiamo creato nel Service
        List<EventResponse> likedEvents = likeService.getLikedEvents(username);
        
        // Restituiamo la risposta usando il metodo 'ok' ereditato da BaseController
        return ok(likedEvents, "Lista eventi con like recuperata con successo");
    }
}
