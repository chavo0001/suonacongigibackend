//LIKE Controller separato per esporre gli endpoint base dei like sugli eventi.
package it.generation.suonacongigi.controller;

import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.dto.like.EventLikeResponse;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.service.EventLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/events/{eventId}/likes")
@RequiredArgsConstructor
public class EventLikeController extends BaseController {

    private final EventLikeService eventLikeService;

    @GetMapping
    public ResponseEntity<ApiEnvelope<EventLikeResponse>> getLikes(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user) {
        String username = user != null ? user.getUsername() : null;
        EventLikeResponse data = eventLikeService.getStatus(Objects.requireNonNull(eventId), username);
        return ok(data, "Like evento recuperati con successo");
    }

    @PostMapping
    public ResponseEntity<ApiEnvelope<EventLikeResponse>> like(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user) {
        String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());
        EventLikeResponse data = eventLikeService.like(Objects.requireNonNull(eventId), username);
        return ok(data, "Like aggiunto con successo");
    }

    @DeleteMapping
    public ResponseEntity<ApiEnvelope<EventLikeResponse>> unlike(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user) {
        String username = Objects.requireNonNull(Objects.requireNonNull(user).getUsername());
        EventLikeResponse data = eventLikeService.unlike(Objects.requireNonNull(eventId), username);
        return ok(data, "Like rimosso con successo");
    }
}
