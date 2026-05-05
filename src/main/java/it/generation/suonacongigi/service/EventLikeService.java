//LIKE Service autonomo per mettere, togliere e leggere i like degli eventi.
package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.like.EventLikeResponse;
import it.generation.suonacongigi.model.Event;
import it.generation.suonacongigi.model.EventLike;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.repository.event.EventLikeRepository;
import it.generation.suonacongigi.repository.event.EventRepository;
import it.generation.suonacongigi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventLikeService {

    private final EventLikeRepository eventLikeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public EventLikeResponse getStatus(Long eventId, @Nullable String username) {
        Event event = getEvent(eventId);
        User user = getUserOrNull(username);
        return toResponse(getEventId(event), user);
    }

    @Transactional
    public EventLikeResponse like(Long eventId, String username) {
        Event event = getEvent(eventId);
        User user = getUser(username);
        Long currentEventId = getEventId(event);
        Long currentUserId = getUserId(user);

        if (!eventLikeRepository.existsByEventIdAndUserId(currentEventId, currentUserId)) {
            //LIKE Certifica il risultato del builder Lombok prima del salvataggio JPA.
            EventLike like = Objects.requireNonNull(EventLike.builder()
                    .event(event)
                    .user(user)
                    .build());
            eventLikeRepository.save(like);
        }

        return toResponse(currentEventId, user);
    }

    @Transactional
    public EventLikeResponse unlike(Long eventId, String username) {
        Event event = getEvent(eventId);
        User user = getUser(username);
        Long currentEventId = getEventId(event);
        Long currentUserId = getUserId(user);

        eventLikeRepository.findByEventIdAndUserId(currentEventId, currentUserId)
                .ifPresent(eventLikeRepository::delete);

        return toResponse(currentEventId, user);
    }

    private EventLikeResponse toResponse(Long eventId, @Nullable User user) {
        boolean likedByCurrentUser = user != null
                && eventLikeRepository.existsByEventIdAndUserId(eventId, getUserId(user));

        //LIKE Certifica il risultato del builder Lombok per rispettare @NonNullApi del package service.
        return Objects.requireNonNull(EventLikeResponse.builder()
                .eventId(eventId)
                .likeCount(eventLikeRepository.countByEventId(eventId))
                .likedByCurrentUser(likedByCurrentUser)
                .build());
    }

    private Event getEvent(Long eventId) {
        //LIKE Certifica il risultato di orElseThrow per rispettare @NonNullApi del package service.
        return Objects.requireNonNull(eventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new NoSuchElementException("Evento non trovato: " + eventId)));
    }

    private User getUser(String username) {
        //LIKE Certifica il risultato di orElseThrow per rispettare @NonNullApi del package service.
        return Objects.requireNonNull(userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato")));
    }

    @Nullable
    private User getUserOrNull(@Nullable String username) {
        if (username == null) {
            return null;
        }

        //LIKE Evita orElse(null) per non creare warning con @NonNullApi e null-safety dell'IDE.
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return null;
        }

        return Objects.requireNonNull(user.get());
    }

    //LIKE Rende esplicito all'IDE che un evento gia salvato deve avere un ID.
    private Long getEventId(Event event) {
        return Objects.requireNonNull(event.getId(), "ID evento mancante");
    }

    //LIKE Rende esplicito all'IDE che un utente gia salvato deve avere un ID.
    private Long getUserId(User user) {
        return Objects.requireNonNull(user.getId(), "ID utente mancante");
    }
}
